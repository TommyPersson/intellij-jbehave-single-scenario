package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import se.fortnox.intellij.jbehave.ui.storyexplorer.preview.ScenarioPreviewPanel
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.Border

class StoryExplorerPanel(project: Project, toolWindow: ToolWindow) : JPanel(BorderLayout()) {

    val storyTree = StoryTree(project, toolWindow)

    val previewPanel = ScenarioPreviewPanel(project, storyTree)

    init {
        val scrollPanel = JBScrollPane(storyTree).also {
            it.border = BorderFactory.createEmptyBorder()
        }

        val toolbar = with (ActionManager.getInstance()) {
            val actionGroup = getAction("StoryExplorerActionGroup") as ActionGroup

            createActionToolbar("StoryExplorer", actionGroup, false).also {
                it.setTargetComponent(this@StoryExplorerPanel)
                it.component.border = makeRightBorder()
            }
        }


        val splitPane = JBSplitter(
            false,
            "StoryExplorerSplitterKey",
            0.5f
        ).also {
            it.firstComponent = scrollPanel
            it.secondComponent = previewPanel.component
            it.isShowDividerControls = true
            it.isShowDividerIcon = true
        }

        add(toolbar.component, BorderLayout.WEST)
        add(splitPane, BorderLayout.CENTER)
    }

    private fun makeRightBorder(): Border {
        return BorderFactory.createMatteBorder(0, 0, 0, 1, JBColor.border())
    }
}