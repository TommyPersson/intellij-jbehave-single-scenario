package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.lang.jvm.actions.stringAttribute
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.roots.ToolbarPanel
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.RunSingleScenarioAction
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel

class StoryExplorerForm(
    private val project: Project,
    private val toolWindow: ToolWindow
) {

    private lateinit var rootPanel: JPanel

    private val treeUpdater: TreeUpdater

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

        treeUpdater = TreeUpdater(storyTree, project, toolWindow)

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