package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import javax.swing.tree.DefaultMutableTreeNode
import java.util.concurrent.atomic.AtomicInteger
import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.*
import com.intellij.ui.treeStructure.Tree
import se.fortnox.intellij.jbehave.ui.storyexplorer.findPsiFile
import se.fortnox.intellij.jbehave.ui.storyexplorer.getAllFilesByExtension
import se.fortnox.intellij.jbehave.ui.storyexplorer.getChildAtAsOrNull
import se.fortnox.intellij.jbehave.ui.storyexplorer.getUserObjectAsOrNull
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode


class StoryTreeUpdater(
    private val storyTree: Tree,
    private val project: Project,
    private val toolWindow: ToolWindow
) {
    private val model = storyTree.model as DefaultTreeModel
    private val root = model.root as DefaultMutableTreeNode

    fun init() {
        PsiManager.getInstance(project).addPsiTreeChangeListener(
            object : PsiTreeChangeAdapter() {
                override fun childrenChanged(event: PsiTreeChangeEvent) {
                    performUpdate()
                    super.childrenChanged(event)
                }
            },
            toolWindow.disposable
        )
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

    private fun makeVisitor(storyIndex: AtomicInteger): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file is StoryFile) {

                    val scenarios = file.steps.map { it.parent }.distinct()

                    val needAdditionalStoryNode = storyIndex.get() >= root.childCount

                    val storyTreeNode = if (needAdditionalStoryNode) {
                        StoryNodeUserData.from(file).wrapInTreeNode().also {
                            root.add(it)
                            model.nodesWereInserted(root, intArrayOf(storyIndex.get()))
                        }
                    } else {
                        root.getChildAtAsOrNull(storyIndex.get())!!
                    }

                    val storyData = storyTreeNode.getUserObjectAsOrNull<StoryNodeUserData>()!!
                    if (storyData.update(file)) {
                        model.nodeChanged(storyTreeNode)
                    }

                    for ((scenarioIndex, scenario) in scenarios.withIndex()) {

                        val needAdditionalScenarioNode = scenarioIndex >= storyTreeNode.childCount

                        val scenarioTreeNode = if (needAdditionalScenarioNode) {
                            ScenarioNodeUserData.from(scenario, storyTree).wrapInTreeNode().also {
                                storyTreeNode.add(it)
                                model.nodesWereInserted(storyTreeNode, intArrayOf(scenarioIndex))
                            }
                        } else {
                            storyTreeNode.getChildAtAsOrNull(scenarioIndex)!!
                        }

                        val scenarioData = scenarioTreeNode.getUserObjectAsOrNull<ScenarioNodeUserData>()!!
                        if (scenarioData.update(scenario)) {
                            model.nodeChanged(scenarioTreeNode)
                        }
                    }

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

                    storyIndex.incrementAndGet()
                } else {
                    super.visitFile(file)
                }
            }
        }
    }
}