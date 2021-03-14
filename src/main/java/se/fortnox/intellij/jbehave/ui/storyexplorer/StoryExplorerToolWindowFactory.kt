package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class StoryExplorerToolWindowFactory : ToolWindowFactory {
    private val title = "Story Explorer"

    override fun isApplicable(project: Project): Boolean {
        // TODO check if any loaded module is JVM based?
        return true
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = with (ContentFactory.SERVICE.getInstance()) {
            val storyExplorer = StoryExplorerPanel(project, toolWindow)
            createContent(storyExplorer, title, false)
        }

        toolWindow.contentManager.addContent(content)
    }

    companion object {
        const val TOOL_WINDOW_ID = "JBehave" // Must match plugin.xml
    }
}