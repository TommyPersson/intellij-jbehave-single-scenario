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
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode


@Suppress("EXPERIMENTAL_API_USAGE")
class StoryTreeUpdater(
    private val storyTree: Tree,
    private val project: Project,
    toolWindow: ToolWindow
) : Disposable {

    private val storyExtension = StoryFileType.STORY_FILE_TYPE.defaultExtension

    private val treeModel get() = storyTree.model as DefaultTreeModel
    private val treeRoot get() = treeModel.root as DefaultMutableTreeNode

    private val updateRequests = MutableSharedFlow<UpdateRequest>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val storyDirectories = mutableListOf<VirtualFile>()

    private var updateJob: Job? = null

    init {
        Disposer.register(toolWindow.disposable, this)

        PsiManager.getInstance(project).addPsiTreeChangeListener(PsiTreeChangeListener(), this)

        updateJob = GlobalScope.launch(Dispatchers.Swing) {
            updateRequests.debounce(100).collect {
                performUpdate(it.reset)
            }
        }

        queueUpdate(true)
    }

    override fun dispose() {
        updateJob?.cancel()
    }

    fun queueUpdate(reset: Boolean = false) {
        updateRequests.tryEmit(UpdateRequest(reset))
    }

    @Suppress("UnstableApiUsage")
    private suspend fun performUpdate(reset: Boolean = false) {
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

    private suspend fun updateTree() {
        storyDirectories.clear()

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
            storyDirectories.add(storyFile.virtualFile.parent)
            updateTreeWithStoryFile(storyFile, storyIndex, moduleNode)
            allowOtherUiTasksToDoWork()
        }

        cleanUpModuleTreeNode(moduleNode, storyFiles)
    }

    private fun updateTreeWithStoryFile(
        storyFile: StoryFile,
        storyIndex: Int,
        moduleNode: DefaultMutableTreeNode
    ) {
        val storyTreeNode = getOrCreateStoryNode(storyFile, storyIndex, moduleNode)

        val storyData = storyTreeNode.getUserObjectAsOrNull<StoryNodeUserData>()!!
        if (storyData.update(storyFile)) {
            treeModel.nodeChanged(storyTreeNode)
        }

        val scenarioElements = findScenarioElements(storyFile)
        for ((scenarioIndex, scenarioElement) in scenarioElements.withIndex()) {
            updateTreeWithScenario(scenarioElement, scenarioIndex, storyTreeNode)
        }

        cleanUpStoryTreeNode(storyTreeNode, scenarioElements)
    }

    private fun updateTreeWithScenario(
        scenarioElement: PsiElement,
        scenarioIndex: Int,
        storyTreeNode: DefaultMutableTreeNode
    ) {
        val scenarioTreeNode = getOrCreateScenarioNode(scenarioIndex, storyTreeNode, scenarioElement)

        val scenarioData = scenarioTreeNode.getUserObjectAsOrNull<ScenarioNodeUserData>()!!
        if (scenarioData.update(scenarioElement)) {
            treeModel.nodeChanged(scenarioTreeNode)
        }
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
        file: PsiFile,
        storyIndex: Int,
        moduleNode: DefaultMutableTreeNode
    ): DefaultMutableTreeNode {
        val needAdditionalStoryNode = storyIndex >= moduleNode.childCount

        return if (needAdditionalStoryNode) {
            StoryNodeUserData.from(file).wrapInTreeNode().also {
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
            val shouldQueueUpdate = event.file is StoryFile

            if (shouldQueueUpdate) {
                queueUpdate()
            }
        }

        override fun childAdded(event: PsiTreeChangeEvent) {
            val shouldQueueUpdate = event.child is StoryFile

            if (shouldQueueUpdate) {
                queueUpdate()
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
                queueUpdate()
            }
        }

        override fun propertyChanged(event: PsiTreeChangeEvent) {
            val shouldQueueUpdate = event.propertyName == PsiTreeChangeEvent.PROP_FILE_TYPES

            if (shouldQueueUpdate) {
                queueUpdate()
            }
        }
    }

    class UpdateRequest(val reset: Boolean)
}