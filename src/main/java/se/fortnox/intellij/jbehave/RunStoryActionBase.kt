package se.fortnox.intellij.jbehave

import com.intellij.execution.Executor
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import se.fortnox.intellij.jbehave.utils.createJUnitConfiguration
import se.fortnox.intellij.jbehave.utils.findJavaTestClass
import javax.swing.Icon


data class StoryFileAndProject(
    val storyFile: VirtualFile,
    val project: Project
)

enum class RunMode {
    Run,
    Debug;

    fun formatActionText(storyFile: VirtualFile?): String {
        return if (storyFile != null) {
            "$this '${storyFile.name}'"
        } else {
            "$this Story"
        }
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
    private val providedStoryFileAndProject: StoryFileAndProject?,
    private val mode: RunMode
) : AnAction() {

    init {
        val text = mode.formatActionText(providedStoryFileAndProject?.storyFile)

        this.templatePresentation.setText(text, false)
        this.templatePresentation.icon = mode.getActionIcon()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val (storyFile, project) = findStoryFileAndProject(e) ?: return

        try {
            val runProfile = createRunProfile(storyFile, project)

            ExecutionEnvironmentBuilder.create(project, mode.executor, runProfile).buildAndExecute()
        } catch (e: Exception) {
            Messages.showMessageDialog(
                project,
                "Failed running story: ${e.message}",
                templatePresentation.text,
                Messages.getInformationIcon()
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val storyFileAndProject = findStoryFileAndProject(e)

        if (storyFileAndProject != null) {
            e.presentation.isEnabledAndVisible = true
            e.presentation.icon = mode.getActionIcon()
            e.presentation.setText(mode.formatActionText(storyFileAndProject.storyFile), false)
        } else {
            e.presentation.isEnabledAndVisible = false
        }
    }

    private fun createRunProfile(storyFile: VirtualFile, project: Project): RunProfile {
        val mainClass = findJavaTestClass(storyFile, project)
            ?: error("Unable to find class file for ${storyFile.presentableName}")

        return with(RunManager.getInstance(project)) {
            val configuration = createJUnitConfiguration(storyFile.presentableName, mainClass)
            addConfiguration(configuration)
            selectedConfiguration = configuration

            configuration.configuration
        }
    }

    private fun findStoryFileAndProject(e: AnActionEvent): StoryFileAndProject? {
        if (providedStoryFileAndProject != null) {
            return providedStoryFileAndProject
        }

        val file = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val project = e.dataContext.getData(CommonDataKeys.PROJECT) ?: return null

        if (file.extension != "story") {
            return null
        }

        return StoryFileAndProject(file, project)
    }
}