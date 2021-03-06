package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.castSafelyTo
import com.intellij.util.ui.tree.TreeUtil
import se.fortnox.intellij.jbehave.ui.storyexplorer.getContextComponent
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

class ExpandAllNodesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val tree = e.getContextComponent<StoryTree>() ?: return
        tree.expandAll()
    }

    private fun JTree.expandAll() {
        TreeUtil.expandAll(this)
    }
}