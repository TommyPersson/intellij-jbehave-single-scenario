package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class StoryExplorerToolWindowFactory : ToolWindowFactory {
    private val title = "Story Explorer"

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = with (ContentFactory.SERVICE.getInstance()) {
            val form = StoryExplorerForm(project, toolWindow)
            createContent(form.content, title, false)
        }

        toolWindow.contentManager.addContent(content)
    }
}