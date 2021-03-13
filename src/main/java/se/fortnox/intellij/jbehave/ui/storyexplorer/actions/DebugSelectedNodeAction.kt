package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData

class DebugSelectedNodeAction : RunSelectedNodeActionBase() {
    override fun getInnerAction(selectionUserData: Any): AnAction? {
        return when (selectionUserData) {
            is ScenarioNodeUserData -> selectionUserData.debugAction
            is StoryNodeUserData -> selectionUserData.debugAction
            else -> null
        }
    }

    companion object {
        val ID: String = DebugSelectedNodeAction::class.java.name
    }
}