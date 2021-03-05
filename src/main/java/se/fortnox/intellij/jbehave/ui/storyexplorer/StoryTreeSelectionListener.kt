package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode

class StoryTreeSelectionListener(
    private val storyTree: Tree
) : TreeSelectionListener {
    override fun valueChanged(e: TreeSelectionEvent) {
        val selectedUserData = e.newLeadSelectionPath.lastPathComponent.castSafelyTo<DefaultMutableTreeNode>()?.userObject
        if (selectedUserData is ScenarioNodeUserData) {
            storyTree.componentPopupMenu = selectedUserData.popupMenu
        } else {
            storyTree.componentPopupMenu = null
        }
    }
}
