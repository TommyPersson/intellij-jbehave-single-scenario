package se.fortnox.intellij.jbehave

import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.execution.Executor
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import se.fortnox.intellij.jbehave.utils.createJUnitConfiguration
import se.fortnox.intellij.jbehave.utils.findJavaTestClass
import javax.swing.Icon

enum class RunMode {
    Run,
    Debug;

    fun formatActionText(storyFile: StoryFile): String {
        return "$this all scenarios in '${storyFile.name}'"
    }

    fun getActionIcon(): Icon {
        return when (this) {
            Run -> AllIcons.Actions.RunAll
            Debug -> AllIcons.Actions.StartDebugger
        }
    }

    val executor: Executor
        get() = when (this) {
        Run -> DefaultRunExecutor.getRunExecutorInstance()
        Debug -> DefaultDebugExecutor.getDebugExecutorInstance()
    }
}


abstract class RunStoryActionBase(
    private val storyFile: StoryFile,
    private val mode: RunMode
) : AnAction(
    mode.formatActionText(storyFile),
    null,
    mode.getActionIcon()
) {
    override fun actionPerformed(e: AnActionEvent) {
        try {
            val runProfile = createRunProfile()

            ExecutionEnvironmentBuilder.create(storyFile.project, mode.executor, runProfile).buildAndExecute()
        } catch (e: Exception) {
            Messages.showMessageDialog(
                storyFile.project,
                "Failed running story: ${e.message}",
                templatePresentation.text,
                Messages.getInformationIcon()
            )
        }
    }

    private fun createRunProfile(): RunProfile {
        val mainClass = storyFile.findJavaTestClass()
            ?: error("Unable to find class file for ${storyFile.name}")

        return with(RunManager.getInstance(storyFile.project)) {
            val configuration = createJUnitConfiguration(storyFile.name, mainClass)
            addConfiguration(configuration)
            selectedConfiguration = configuration

            configuration.configuration
        }
    }
}