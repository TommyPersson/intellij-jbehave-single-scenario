package se.fortnox.intellij.jbehave

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import se.fortnox.intellij.jbehave.utils.findStoryClass

class GoToStoryClassAction(
    private val project: Project? = null,
    private val storyFile: VirtualFile? = null,
) : AnAction(
    "Go to Story Class",
    "Go to story class",
    AllIcons.Actions.EditSource
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = findProject(e) ?: return
        val storyFile = findStoryFile(e) ?: return

        val storyClass = findStoryClass(storyFile, project)
        if (storyClass == null) {
            Messages.showMessageDialog(
                project,
                "Unable to find class file for ${storyFile.presentableName}",
                templatePresentation.text,
                Messages.getInformationIcon()
            )
            return
        }

        storyClass.navigate(true)
    }

    private fun findProject(e: AnActionEvent): Project? {
        return project ?: e.project
    }

    override fun update(e: AnActionEvent) {
        val storyFile = findStoryFile(e)

        val showAction = storyFile != null
        e.presentation.isEnabledAndVisible = showAction
    }

    private fun findStoryFile(e: AnActionEvent): VirtualFile? {
        if (storyFile != null) {
            return storyFile
        }

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if (file.extension != "story") {
            return null
        }

        return file
    }

    companion object {
        val ID: String = GoToStoryClassAction::class.java.name
    }
}