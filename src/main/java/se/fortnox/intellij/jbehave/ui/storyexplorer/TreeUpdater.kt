package se.fortnox.intellij.jbehave.ui.storyexplorer

import javax.swing.tree.DefaultMutableTreeNode
import java.util.concurrent.atomic.AtomicInteger
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode


class TreeUpdater(
    private val storyTree: Tree,
    private val project: Project
) {
    fun performUpdate() {
        val storyIndex = AtomicInteger(0)

        for (storyVirtualFile in project.getAllFilesByExtension("story")) {
            project.findPsiFile(storyVirtualFile)?.accept(makeVisitor(storyIndex))
        }
    }

    private fun makeVisitor(storyIndex: AtomicInteger): PsiElementVisitor {
        val model = storyTree.model as DefaultTreeModel
        val root = model.root as DefaultMutableTreeNode

        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file is StoryFile) {
                    val scenarios = file.steps.map { it.parent }.distinct()

                    val needAdditionalStoryNode = storyIndex.get() >= root.childCount

                    val storyTreeNode = if (needAdditionalStoryNode) {
                        StoryNodeData.from(file).wrapInTreeNode().also {
                            root.add(it)
                            model.nodesWereInserted(root, intArrayOf(storyIndex.get()))
                        }
                    } else {
                        root.getChildAtAs(storyIndex.get())
                    }

                    val storyData = storyTreeNode.getUserObjectAs<StoryNodeData>()
                    if (storyData.update(file)) {
                        model.nodeChanged(storyTreeNode)
                    }

                    for ((scenarioIndex, scenario) in scenarios.withIndex()) {

                        val needAdditionalScenarioNode = scenarioIndex >= storyTreeNode.childCount

                        val scenarioTreeNode = if (needAdditionalScenarioNode) {
                            ScenarioNodeData.from(scenario).wrapInTreeNode().also {
                                storyTreeNode.add(it)
                                model.nodesWereInserted(storyTreeNode, intArrayOf(scenarioIndex))
                            }
                        } else {
                            storyTreeNode.getChildAtAs(scenarioIndex)
                        }

                        val scenarioData = scenarioTreeNode.getUserObjectAs<ScenarioNodeData>()
                        if (scenarioData.update(scenario)) {
                            model.nodeChanged(scenarioTreeNode)
                        }
                    }

                    val numScenarioNodes = storyTreeNode.childCount
                    val numScenarios = scenarios.size

                    if (numScenarioNodes > numScenarios) {
                        for (i in numScenarios until numScenarioNodes) {
                            val scenarioNodeToRemove = storyTreeNode.getChildAtAs<MutableTreeNode>(numScenarios)
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

inline fun <reified T : TreeNode> TreeNode.getChildAtAs(index: Int): T {
    return this.getChildAt(index) as T
}

inline fun <reified T> DefaultMutableTreeNode.getUserObjectAs(): T {
    return this.userObject as T
}

fun Project.findPsiFile(virtualFile: VirtualFile): PsiFile? {
    return PsiManager.getInstance(this).findFile(virtualFile)
}

fun Project.getAllFilesByExtension(extension: String): Collection<VirtualFile> {
    return FilenameIndex.getAllFilesByExt(this, extension)
}