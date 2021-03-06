package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode

class StoryTreeSelectionListener(
    private val storyTree: Tree
) : TreeSelectionListener {
    override fun valueChanged(e: TreeSelectionEvent) {
        val selectedUserData = e.newLeadSelectionPath?.lastPathComponent?.castSafelyTo<DefaultMutableTreeNode>()
            ?.userObject

        storyTree.componentPopupMenu = when (selectedUserData) {
            is ScenarioNodeUserData -> selectedUserData.popupMenu
            is StoryNodeUserData -> selectedUserData.popupMenu
            else -> null
        }
    }
}
