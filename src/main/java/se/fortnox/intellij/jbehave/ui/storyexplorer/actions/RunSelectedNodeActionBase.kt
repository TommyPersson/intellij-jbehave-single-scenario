package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import se.fortnox.intellij.jbehave.ui.storyexplorer.StoryExplorerPanel
import se.fortnox.intellij.jbehave.utils.getContextComponent

abstract class RunSelectedNodeActionBase : AnAction() {

    private var innerAction: AnAction? = null

    override fun actionPerformed(e: AnActionEvent) {
        innerAction?.actionPerformed(e)
    }

    override fun update(e: AnActionEvent) {
        val tree = e.getContextComponent<StoryExplorerPanel>()?.storyTree
            ?: return

        val action = tree.getSelectionUserData()?.let {
            getInnerAction(it)
        }

        innerAction = action

        if (action != null) {
            e.presentation.isEnabled = true
            e.presentation.text = action.templatePresentation.text
            e.presentation.description = action.templatePresentation.description
        } else {
            e.presentation.isEnabled = false
            e.presentation.text = this.templatePresentation.text
            e.presentation.description = this.templatePresentation.description
        }
    }

    abstract fun getInnerAction(selectionUserData: Any): AnAction?
}