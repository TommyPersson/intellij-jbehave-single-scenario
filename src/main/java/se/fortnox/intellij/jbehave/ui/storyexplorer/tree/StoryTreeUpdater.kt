package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.github.kumaraman21.intellijbehave.parser.StoryElementType
import javax.swing.tree.DefaultMutableTreeNode
import java.util.concurrent.atomic.AtomicInteger
import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.*
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.elementType
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.Alarm
import se.fortnox.intellij.jbehave.ui.storyexplorer.*
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ModuleNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.wrapInTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode


class StoryTreeUpdater(
    private val storyTree: Tree,
    private val project: Project,
    private val toolWindow: ToolWindow
) {
    private val model = storyTree.model as DefaultTreeModel
    private val root = model.root as DefaultMutableTreeNode

    private val updateAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, toolWindow.disposable)

    fun init() {
        PsiManager.getInstance(project).addPsiTreeChangeListener(
            object : PsiTreeChangeAdapter() {
                override fun childrenChanged(event: PsiTreeChangeEvent) {
                    queueUpdate()
                }
            },
            toolWindow.disposable
        )

        queueUpdate(true)
    }

    fun performUpdate(reset: Boolean = false) {
        if (reset) {
            root.removeAllChildren()
            model.reload()
        }

        val storyIndex = AtomicInteger(0)

        for (storyVirtualFile in project.getAllFilesByExtension("story")) {
            project.findPsiFile(storyVirtualFile)?.accept(makeVisitor(storyIndex))
        }
    }

    fun queueUpdate(reset: Boolean = false) {
        updateAlarm.cancelAllRequests()
        updateAlarm.addRequest({
            performUpdate(reset)
        }, 200)
    }

    private fun makeVisitor(storyIndex: AtomicInteger): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is StoryFile) {
                    return
                }

                val moduleNode = getOrCreateModuleNode(file)

                val storyTreeNode = getOrCreateStoryNode(file, storyIndex.getAndIncrement(), moduleNode)
                updateStoryTreeNode(storyTreeNode, file)

                val scenarioElements = findScenarioElements(file)
                for ((scenarioIndex, scenarioElement) in scenarioElements.withIndex()) {
                    val scenarioTreeNode = getOrCreateScenarioNode(scenarioElement, scenarioIndex, storyTreeNode)
                    updateScenarioNode(scenarioTreeNode, scenarioElement)
                }

                cleanUpStoryTreeNode(storyTreeNode, scenarioElements)
            }
        }
    }

    private fun findScenarioElements(file: StoryFile): List<PsiElement> {
        return file.collectDescendantsOfType { it.elementType == StoryElementType.SCENARIO }
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
                model.nodesWereRemoved(
                    storyTreeNode,
                    intArrayOf(numScenarios),
                    arrayOf(scenarioNodeToRemove)
                )
            }
        }
    }

    private fun updateStoryTreeNode(
        storyTreeNode: DefaultMutableTreeNode,
        file: PsiFile
    ) {
        val storyData = storyTreeNode.getUserObjectAsOrNull<StoryNodeUserData>()!!
        if (storyData.update(file)) {
            model.nodeChanged(storyTreeNode)
        }
    }

    private fun updateScenarioNode(
        scenarioTreeNode: DefaultMutableTreeNode,
        scenario: PsiElement
    ) {
        val scenarioData = scenarioTreeNode.getUserObjectAsOrNull<ScenarioNodeUserData>()!!
        if (scenarioData.update(scenario)) {
            model.nodeChanged(scenarioTreeNode)
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
                model.nodesWereInserted(moduleNode, intArrayOf(moduleNode.childCount - 1))
            }
        } else {
            moduleNode.getChildAtAsOrNull(storyIndex)!!
        }
    }

    private fun getOrCreateScenarioNode(
        scenario: PsiElement,
        scenarioIndex: Int,
        storyTreeNode: DefaultMutableTreeNode
    ): DefaultMutableTreeNode {
        val needAdditionalScenarioNode = scenarioIndex >= storyTreeNode.childCount

        return if (needAdditionalScenarioNode) {
            ScenarioNodeUserData.from(scenario, storyTree).wrapInTreeNode().also {
                storyTreeNode.add(it)
                model.nodesWereInserted(storyTreeNode, intArrayOf(storyTreeNode.childCount - 1))
            }
        } else {
            storyTreeNode.getChildAtAsOrNull(scenarioIndex)!!
        }
    }

    private fun getOrCreateModuleNode(
        file: PsiFile,
    ): DefaultMutableTreeNode {
        val module = file.containingModule!!

        val existingTreeNode = root.children().toList()
            .filterIsInstance<DefaultMutableTreeNode>()
            .firstOrNull { it.getUserObjectAsOrNull<ModuleNodeUserData>()?.module == module }

        return if (existingTreeNode != null) {
            // TODO update node
            existingTreeNode
        } else {
            ModuleNodeUserData.from(module).wrapInTreeNode().also {
                root.add(it)
                model.nodesWereInserted(root, intArrayOf(root.childCount - 1))
            }
        }
    }
}