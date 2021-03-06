package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.ui.tree.TreeUtil
import se.fortnox.intellij.jbehave.ui.storyexplorer.getContextComponent
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import javax.swing.JTree

class CollapseAllNodesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val tree = e.getContextComponent<StoryTree>() ?: return
        tree.collapseAll()
    }

    private fun JTree.collapseAll() {
        TreeUtil.collapseAll(this, false, 0)
    }
}