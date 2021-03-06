package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.StoryExplorerPanel
import se.fortnox.intellij.jbehave.ui.storyexplorer.getContextComponent

class TogglePreviewAction : ToggleAction() {

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val previewPanel = e.getContextComponent<StoryExplorerPanel>()?.previewPanel
            ?: return

        previewPanel.shouldShowPreview = state
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        val previewPanel = e.getContextComponent<StoryExplorerPanel>()?.previewPanel
            ?: return false

        return previewPanel.shouldShowPreview
    }
}