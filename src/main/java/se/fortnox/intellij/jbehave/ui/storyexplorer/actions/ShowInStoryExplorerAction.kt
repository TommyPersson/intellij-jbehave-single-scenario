package se.fortnox.intellij.jbehave.ui.storyexplorer.actions

import com.github.kumaraman21.intellijbehave.parser.StoryElementType
import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.psi.PsiElement
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.StoryExplorerToolWindowFactory
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import se.fortnox.intellij.jbehave.utils.findPsiFile

class ShowInStoryExplorerAction : AnAction(
    "Show in Story Explorer",
    "Show in story explorer",
    AllIcons.General.Locate
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val navigationRequest = createNavigationRequest(e, project)

        if (navigationRequest != null) {
            val publisher = getPublisher(project)
            val toolWindow = getToolWindow(project)

            toolWindow?.show {
                publisher.requestNavigation(navigationRequest)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return

        val navigationRequest = createNavigationRequest(e, project)
        if (navigationRequest != null) {
            e.presentation.isEnabledAndVisible = true
        }
    }

    private fun createNavigationRequest(e: AnActionEvent, project: Project): StoryTree.NavigationRequest? {
        return when (e.place) {
            ActionPlaces.PROJECT_VIEW_POPUP -> createNavigationRequestFromProjectView(e)
            ActionPlaces.EDITOR_POPUP -> createNavigationRequestFromEditor(e)
            else -> null
        }
    }

    private fun createNavigationRequestFromProjectView(e: AnActionEvent): StoryTree.NavigationRequest? {
        val storyFile = findStoryFile(e) ?: return null

        return StoryTree.NavigationRequest.ToStory(storyFile)
    }

    private fun createNavigationRequestFromEditor(e: AnActionEvent): StoryTree.NavigationRequest? {
        val storyFile = findStoryFile(e) ?: return null
        val scenarioElement = findScenarioElement(e, storyFile) ?: return null

        return StoryTree.NavigationRequest.ToScenario(storyFile, scenarioElement)
    }

    private fun findStoryFile(e: AnActionEvent): StoryFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if (file.extension != "story") {
            return null
        }

        return e.project?.findPsiFile(file)?.castSafelyTo<StoryFile>()
    }

    private fun findScenarioElement(e: AnActionEvent, storyFile: StoryFile): PsiElement? {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val primaryCaret = editor.caretModel.primaryCaret
        val start = primaryCaret.selectionStart

        val element = storyFile.findElementAt(start) ?: return null
        return findClosestScenarioElement(element)
    }

    private fun findClosestScenarioElement(element: PsiElement?): PsiElement? {
        if (element == null) {
            return null
        }

        if (element.node?.elementType != StoryElementType.SCENARIO) {
            return findClosestScenarioElement(element.parent)
        }

        return element
    }

    private fun getPublisher(project: Project) =
        project.messageBus.syncPublisher(StoryTree.NavigationNotifier.MESSAGE_BUS_TOPIC)

    private fun getToolWindow(project: Project) =
        ToolWindowManagerEx.getInstanceEx(project).getToolWindow(StoryExplorerToolWindowFactory.TOOL_WINDOW_ID)

    companion object {
        val ID: String = ShowInStoryExplorerAction::class.java.name
    }
}