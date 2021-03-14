package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.github.kumaraman21.intellijbehave.language.StoryFileType
import com.github.kumaraman21.intellijbehave.parser.StoryElementType
import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.*
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.elementType
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import com.intellij.util.ui.tree.TreeUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.swing.Swing
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ModuleNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.wrapInTreeNode
import se.fortnox.intellij.jbehave.utils.*
import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode


@Suppress("EXPERIMENTAL_API_USAGE")
class StoryTreeUpdater(
    private val storyTree: Tree,
    private val project: Project,
    toolWindow: ToolWindow
) : Disposable {

    private sealed class UpdateRequest {
        object Reset : UpdateRequest()
        object Normal : UpdateRequest()
        class SingleStory(val storyFile: StoryFile) : UpdateRequest()
    }

    private val storyExtension = StoryFileType.STORY_FILE_TYPE.defaultExtension

    private val treeModel get() = storyTree.model as DefaultTreeModel
    private val treeRoot get() = treeModel.root as DefaultMutableTreeNode

    private val updateRequests = MutableSharedFlow<UpdateRequest>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val storyDirectories = mutableListOf<VirtualFile>()
    private val storyFileNodeIndex = WeakHashMap<StoryFile, DefaultMutableTreeNode>()
    private val scenarioElementNodeIndex = WeakHashMap<PsiElement, DefaultMutableTreeNode>()

    private var updateJob: Job? = null

    init {
        Disposer.register(toolWindow.disposable, this)

        project.addPsiTreeChangeListener(PsiTreeChangeListener(), this)

        updateJob = GlobalScope.launch(Dispatchers.Swing) {
            updateRequests.debounce(100).collect {
                handleUpdateRequest(it)
            }
        }

        queueFullTreeReset()
    }

    override fun dispose() {
        updateJob?.cancel()
    }

    fun queueFullTreeReset() {
        updateRequests.tryEmit(UpdateRequest.Reset)
    }

    fun findStoryNode(storyFile: StoryFile): DefaultMutableTreeNode? {
        return storyFileNodeIndex[storyFile]
    }

    fun findScenarioNode(scenarioElement: PsiElement): DefaultMutableTreeNode? {
        return scenarioElementNodeIndex[scenarioElement]
    }

    private fun queueFullTreeUpdate() {
        updateRequests.tryEmit(UpdateRequest.Normal)
    }

    private fun queueSingleStoryNodeUpdate(storyFile: StoryFile) {
        updateRequests.tryEmit(UpdateRequest.SingleStory(storyFile))
    }

    private suspend fun handleUpdateRequest(updateRequest: UpdateRequest) {
        when (updateRequest) {
            is UpdateRequest.Reset -> performFullTreeUpdate(true)
            is UpdateRequest.Normal -> performFullTreeUpdate(false)
            is UpdateRequest.SingleStory -> performSingleStoryUpdate(updateRequest.storyFile)
        }
    }

    private suspend fun performFullTreeUpdate(reset: Boolean = false) {
        if (reset) {
            treeRoot.removeAllChildren()
            treeModel.reload()
        }

        updateTree()

        if (reset) {
            TreeUtil.expandAll(storyTree)
        } else {
            storyTree.expandRow(0)
        }
    }

    private suspend fun performSingleStoryUpdate(storyFile: StoryFile) {
        val storyNode = storyFileNodeIndex[storyFile]
        if (storyNode == null) {
            performFullTreeUpdate(false)
            return
        }

        val moduleNode = storyNode.parent as DefaultMutableTreeNode
        val indexOfStoryNode = moduleNode.getIndex(storyNode)

        updateTreeWithStoryFile(storyFile, indexOfStoryNode, moduleNode)
    }

    private suspend fun updateTree() {
        storyDirectories.clear()
        storyFileNodeIndex.clear()
        scenarioElementNodeIndex.clear()

        val modules = project.modules.sortedBy { it.contentRootPath }
        for (module in modules) {
            updateTreeWithModule(module)
            allowOtherUiTasksToDoWork()
        }
    }

    private suspend fun updateTreeWithModule(module: Module) {
        val moduleNode = getOrCreateModuleNode(module)

        val storyFiles = module.getAllFilesByExtension(storyExtension)
            .sortedBy { it.path }
            .mapNotNull { project.findPsiFile(it) }
            .mapNotNull { it.castSafelyTo<StoryFile>() }

        for ((storyIndex, storyFile) in storyFiles.withIndex()) {
            val storyNode = updateTreeWithStoryFile(storyFile, storyIndex, moduleNode)

            storyDirectories.add(storyFile.virtualFile.parent)
            storyFileNodeIndex[storyFile] = storyNode

            allowOtherUiTasksToDoWork()
        }

        cleanUpModuleTreeNode(moduleNode, storyFiles)
    }

    private fun updateTreeWithStoryFile(
        storyFile: StoryFile,
        storyIndex: Int,
        moduleNode: DefaultMutableTreeNode
    ): DefaultMutableTreeNode {
        val storyTreeNode = getOrCreateStoryNode(storyFile, storyIndex, moduleNode)

        val storyData = storyTreeNode.getUserObjectAsOrNull<StoryNodeUserData>()!!
        if (storyData.update(storyFile)) {
            treeModel.nodeChanged(storyTreeNode)
        }

        val scenarioElements = findScenarioElements(storyFile)
        for ((scenarioIndex, scenarioElement) in scenarioElements.withIndex()) {
            val scenarioNode = updateTreeWithScenario(scenarioElement, scenarioIndex, storyTreeNode)
            scenarioElementNodeIndex[scenarioElement] = scenarioNode
        }

        cleanUpStoryTreeNode(storyTreeNode, scenarioElements)

        return storyTreeNode
    }

    private fun updateTreeWithScenario(
        scenarioElement: PsiElement,
        scenarioIndex: Int,
        storyTreeNode: DefaultMutableTreeNode
    ): DefaultMutableTreeNode {
        val scenarioTreeNode = getOrCreateScenarioNode(scenarioIndex, storyTreeNode, scenarioElement)

        val scenarioData = scenarioTreeNode.getUserObjectAsOrNull<ScenarioNodeUserData>()!!
        if (scenarioData.update(scenarioElement)) {
            treeModel.nodeChanged(scenarioTreeNode)
        }

        return scenarioTreeNode
    }

    private fun findScenarioElements(file: StoryFile): List<PsiElement> {
        return file.collectDescendantsOfType { it.elementType == StoryElementType.SCENARIO }
    }

    private fun getOrCreateModuleNode(
        module: Module,
    ): DefaultMutableTreeNode {
        val existingModuleNode = treeRoot.children().toList()
            .filterIsInstance<DefaultMutableTreeNode>()
            .firstOrNull { it.getUserObjectAsOrNull<ModuleNodeUserData>()?.module == module }

        return existingModuleNode
            ?: ModuleNodeUserData.from(module).wrapInTreeNode().also {
                treeRoot.add(it)
                treeModel.nodesWereInserted(treeRoot, intArrayOf(treeRoot.childCount - 1))
            }
    }

    private fun getOrCreateStoryNode(
        file: StoryFile,
        storyIndex: Int,
        moduleNode: DefaultMutableTreeNode
    ): DefaultMutableTreeNode {
        val needAdditionalStoryNode = storyIndex >= moduleNode.childCount

        return if (needAdditionalStoryNode) {
            StoryNodeUserData.from(file, storyTree).wrapInTreeNode().also {
                moduleNode.add(it)
                treeModel.nodesWereInserted(moduleNode, intArrayOf(moduleNode.childCount - 1))
            }
        } else {
            moduleNode.getChildAtAsOrNull(storyIndex)!!
        }
    }

    private fun getOrCreateScenarioNode(
        scenarioIndex: Int,
        storyTreeNode: DefaultMutableTreeNode,
        scenarioElement: PsiElement
    ): DefaultMutableTreeNode {
        val needAdditionalScenarioNode = scenarioIndex >= storyTreeNode.childCount

        return if (needAdditionalScenarioNode) {
            ScenarioNodeUserData.from(scenarioElement, storyTree).wrapInTreeNode().also {
                storyTreeNode.add(it)
                treeModel.nodesWereInserted(storyTreeNode, intArrayOf(storyTreeNode.childCount - 1))
            }
        } else {
            storyTreeNode.getChildAtAsOrNull(scenarioIndex)!!
        }
    }

    private fun cleanUpModuleTreeNode(
        moduleTreeNode: DefaultMutableTreeNode,
        stories: List<PsiFile>
    ) {
        val numStoryNodes = moduleTreeNode.childCount
        val numStories = stories.size

        if (numStoryNodes > numStories) {
            for (i in numStories until numStoryNodes) {
                val storyNodeToRemove = moduleTreeNode.getChildAtAsOrNull<MutableTreeNode>(numStories)
                moduleTreeNode.remove(storyNodeToRemove)
                treeModel.nodesWereRemoved(
                    moduleTreeNode,
                    intArrayOf(numStories),
                    arrayOf(storyNodeToRemove)
                )
            }
        }
    }

    private fun cleanUpStoryTreeNode(
        storyTreeNode: DefaultMutableTreeNode,
        scenarios: List<PsiElement>
    ) {
        val numScenarioNodes = storyTreeNode.childCount
        val numScenarios = scenarios.size

        if (numScenarioNodes > numScenarios) {
            for (i in numScenarios until numScenarioNodes) {
                val scenarioNodeToRemove = storyTreeNode.getChildAtAsOrNull<MutableTreeNode>(numScenarios)
                storyTreeNode.remove(scenarioNodeToRemove)
                treeModel.nodesWereRemoved(
                    storyTreeNode,
                    intArrayOf(numScenarios),
                    arrayOf(scenarioNodeToRemove)
                )
            }
        }
    }

    private suspend fun allowOtherUiTasksToDoWork() {
        delay(0)
    }

    private inner class PsiTreeChangeListener : PsiTreeChangeAdapter() {
        override fun childrenChanged(event: PsiTreeChangeEvent) {
            val file = event.file
            if (file is StoryFile) {
                queueSingleStoryNodeUpdate(file)
            }
        }

        override fun childAdded(event: PsiTreeChangeEvent) {
            val shouldQueueUpdate = event.child is StoryFile

            if (shouldQueueUpdate) {
                queueFullTreeUpdate()
            }
        }

        override fun childRemoved(event: PsiTreeChangeEvent) {
            val child = event.child

            val shouldQueueUpdate = when (child) {
                is StoryFile -> true
                is PsiDirectory -> storyDirectories.contains(child.virtualFile)
                else -> false
            }

            if (shouldQueueUpdate) {
                queueFullTreeUpdate()
            }
        }

        override fun propertyChanged(event: PsiTreeChangeEvent) {
            val shouldQueueUpdate = event.propertyName == PsiTreeChangeEvent.PROP_FILE_TYPES

            if (shouldQueueUpdate) {
                queueFullTreeUpdate()
            }
        }
    }
}