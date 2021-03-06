package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.Border

class StoryExplorerForm(
    private val project: Project,
    private val toolWindow: ToolWindow
) {
    private lateinit var rootPanel: JPanel

    val content: JComponent get() = rootPanel

    init {
        val storyTree = StoryTree(project, toolWindow)

        val scrollPanel = JBScrollPane(storyTree).also {
            it.border = BorderFactory.createEmptyBorder()
        }

        val toolbar = with (ActionManager.getInstance()) {
            val actionGroup = getAction("StoryExplorerActionGroup") as ActionGroup

            createActionToolbar("StoryExplorer", actionGroup, false).also {
                it.setTargetComponent(storyTree)
                it.component.border = makeRightBorder()
            }
        }

        rootPanel.add(toolbar.component, BorderLayout.WEST)
        rootPanel.add(scrollPanel, BorderLayout.CENTER)
    }

    private fun makeRightBorder(): Border = BorderFactory.createMatteBorder(0, 0, 0, 1, JBColor.border())
}

