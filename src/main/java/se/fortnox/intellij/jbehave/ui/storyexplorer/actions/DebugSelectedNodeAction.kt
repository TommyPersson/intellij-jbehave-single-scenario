package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData

class DebugSelectedNodeAction : RunSelectedNodeActionBase() {
    override fun getInnerAction(selectionUserData: Any): AnAction? {
        return when (selectionUserData) {
            is ScenarioNodeUserData -> selectionUserData.debugAction
            else -> null
        }
    }

    companion object {
        val ID: String = DebugSelectedNodeAction::class.java.name
    }
}