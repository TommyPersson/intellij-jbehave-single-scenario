package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.intellij.openapi.actionSystem.AnAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData

class RunSelectedNodeAction : RunSelectedNodeActionBase() {
    override fun getInnerAction(selectionUserData: Any): AnAction? {
        return when (selectionUserData) {
            is ScenarioNodeUserData -> selectionUserData.runAction
            else -> null
        }
    }
}

