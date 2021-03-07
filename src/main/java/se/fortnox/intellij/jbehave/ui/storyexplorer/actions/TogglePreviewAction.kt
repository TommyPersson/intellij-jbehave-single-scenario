package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.preview.ScenarioPreviewPanel

class TogglePreviewAction(
    private val previewPanel: ScenarioPreviewPanel
) : ToggleAction(
    "Show preview",
    null,
    AllIcons.Actions.PreviewDetails
) {
    override fun setSelected(e: AnActionEvent, state: Boolean) {
        previewPanel.shouldShowPreview = state
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return previewPanel.shouldShowPreview
    }
}
