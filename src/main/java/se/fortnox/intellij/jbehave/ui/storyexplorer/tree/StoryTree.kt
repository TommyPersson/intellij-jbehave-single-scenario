package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.RootStoryNodeUserUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeCellRenderer
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeMouseListener
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeSelectionListener
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTreeUpdater
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel

class StoryTree(project: Project, toolWindow: ToolWindow) : Tree() {
    private val updater = StoryTreeUpdater(this, project, toolWindow)

    init {
        model.root.castSafelyTo<DefaultMutableTreeNode>()?.userObject = RootStoryNodeUserUserData("All Stories")
        cellRenderer = StoryTreeCellRenderer()
        addTreeSelectionListener(StoryTreeSelectionListener(this))
        addMouseListener(StoryTreeMouseListener(this))
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        updater.init()
    }

    fun refresh() {
        updater.performUpdate(reset = true)
    }

    fun getSelectionUserData(): Any? {
        return selectionModel?.selectionPath?.lastPathComponent?.castSafelyTo<DefaultMutableTreeNode>()?.userObject
    }
}