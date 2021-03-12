package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.github.kumaraman21.intellijbehave.language.StoryFileType
import com.github.kumaraman21.intellijbehave.parser.StoryElementType
import javax.swing.tree.DefaultMutableTreeNode
import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.*
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.elementType
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.Alarm
import com.intellij.util.castSafelyTo
import com.intellij.util.ui.tree.TreeUtil
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ModuleNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.wrapInTreeNode
import se.fortnox.intellij.jbehave.utils.*
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode


class StoryTreeUpdater(
    private val storyTree: Tree,
    private val project: Project,
    private val toolWindow: ToolWindow
) {
    private val storyExtension = StoryFileType.STORY_FILE_TYPE.defaultExtension

    private val treeModel get() = storyTree.model as DefaultTreeModel
    private val treeRoot get() = treeModel.root as DefaultMutableTreeNode

    private val updateAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, toolWindow.disposable)

    private val storyDirectories = mutableListOf<VirtualFile>()

    fun init() {
        PsiManager.getInstance(project).addPsiTreeChangeListener(PsiTreeChangeListener(), toolWindow.disposable)
        queueUpdate(true)
    }

    fun queueUpdate(reset: Boolean = false) {
        updateAlarm.cancelAllRequests()
        updateAlarm.addRequest({
            performUpdate(reset)
        }, 100)
    }

    fun performUpdate(reset: Boolean = false) {
        // Needs to be 'true' while updating the tree?
        // Just setting false in StoryTree.init results in a blank tree
        storyTree.isRootVisible = true

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

        storyTree.isRootVisible = false
    }

    private fun updateTree() {
        storyDirectories.clear()

        val modules = project.modules.sortedBy { it.contentRootPath }
        for (module in modules) {
            updateTreeWithModule(module)
        }
    }

    private fun updateTreeWithModule(module: Module) {
        val moduleNode = getOrCreateModuleNode(module)

        val storyFiles = module.getAllFilesByExtension(storyExtension)
            .sortedBy { it.path }
            .mapNotNull { project.findPsiFile(it) }
            .mapNotNull { it.castSafelyTo<StoryFile>() }

        storyDirectories.addAll(storyFiles.map { it.virtualFile.parent })

        for ((storyIndex, storyFile) in storyFiles.withIndex()) {
            updateTreeWithStoryFile(storyFile, storyIndex, moduleNode)
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
    }
}