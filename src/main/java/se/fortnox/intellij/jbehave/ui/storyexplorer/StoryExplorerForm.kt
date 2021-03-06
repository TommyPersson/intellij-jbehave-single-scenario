package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.RootStoryNodeUserUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeCellRenderer
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeMouseListener
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeSelectionListener
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeUpdater
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel

class StoryExplorerForm(
    private val project: Project,
    private val toolWindow: ToolWindow
) {

    private lateinit var rootPanel: JPanel

    private val storyTreeUpdater: StoryTreeUpdater

    val content: JComponent get() = rootPanel

    init {
        val storyTree = Tree().also {
            it.model.root.castSafelyTo<DefaultMutableTreeNode>()?.userObject = RootStoryNodeUserUserData("All Stories")
            it.cellRenderer = StoryTreeCellRenderer()
            it.addTreeSelectionListener(StoryTreeSelectionListener(it))
            it.addMouseListener(StoryTreeMouseListener(it))
            it.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        }

        val scrollPanel = JBScrollPane(storyTree).also {
            it.border = BorderFactory.createEmptyBorder()
        }

        storyTreeUpdater = StoryTreeUpdater(storyTree, project, toolWindow)

        rootPanel.add(scrollPanel, fillingGridBagConstraints)
    }
}

val fillingGridBagConstraints = GridBagConstraints(
    0,
    GridBagConstraints.RELATIVE,
    1,
    1,
    1.0,
    1.0,
    GridBagConstraints.WEST,
    GridBagConstraints.BOTH,
    Insets(0, 0, 0, 0),
    0,
    0
)