package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree

class RefreshStoryTreeAction(
    private val storyTree: StoryTree
) : AnAction(
    "Refresh",
    "Refresh the story explorer",
    AllIcons.Actions.Refresh
) {
    override fun actionPerformed(e: AnActionEvent) {
        storyTree.refresh()
    }
}