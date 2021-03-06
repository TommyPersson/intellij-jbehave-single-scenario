package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import se.fortnox.intellij.jbehave.ui.storyexplorer.getContextComponent

class RefreshStoryTreeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getContextComponent<StoryTree>()?.refresh()
    }
}