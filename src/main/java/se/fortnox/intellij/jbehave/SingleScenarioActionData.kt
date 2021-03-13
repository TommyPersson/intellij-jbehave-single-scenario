package se.fortnox.intellij.jbehave

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import se.fortnox.intellij.jbehave.utils.ScenarioUtils

class SingleScenarioActionData(
    val storyFile: VirtualFile,
    val project: Project,
    val scenarioTitle: String,
) {
    fun formatActionText(actionPrefix: String): String {
        return "$actionPrefix '${ScenarioUtils.formatTrimmedTitle(scenarioTitle)}'"
    }
}