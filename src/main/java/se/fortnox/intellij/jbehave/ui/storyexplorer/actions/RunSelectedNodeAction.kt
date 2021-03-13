package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData

class RunSelectedNodeAction : RunSelectedNodeActionBase() {
    override fun getInnerAction(selectionUserData: Any): AnAction? {
        return when (selectionUserData) {
            is ScenarioNodeUserData -> selectionUserData.runAction
            is StoryNodeUserData -> selectionUserData.runAction
            else -> null
        }
    }

    companion object {
        val ID: String = RunSelectedNodeAction::class.java.name
    }
}

