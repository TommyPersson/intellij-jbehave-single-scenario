package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.ide.CommonActionsManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import se.fortnox.intellij.jbehave.ui.storyexplorer.actions.DebugSelectedNodeAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.actions.RefreshStoryTreeAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.actions.RunSelectedNodeAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.actions.TogglePreviewAction
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
        val scrollPane = createScrollPane()
        val toolbar = createToolbar()
        val splitPane = createSplitPane(scrollPane, previewPanel.component)

        add(toolbar.component, BorderLayout.WEST)
        add(splitPane, BorderLayout.CENTER)
    }

    private fun createScrollPane(): JBScrollPane {
        return JBScrollPane(storyTree).also {
            it.border = BorderFactory.createEmptyBorder()
        }
    }

    private fun createToolbar(): ActionToolbar {
        val actionManager = ActionManager.getInstance()
        val commonActionManager = CommonActionsManager.getInstance()

        val actionGroup = DefaultActionGroup().also {
            it.add(RefreshStoryTreeAction(storyTree))
            it.add(Separator())
            it.add(actionManager.getAction(RunSelectedNodeAction.ID))
            it.add(actionManager.getAction(DebugSelectedNodeAction.ID))
            it.add(Separator())
            it.add(commonActionManager.createExpandAllAction(storyTree.treeExpander, storyTree))
            it.add(commonActionManager.createCollapseAllAction(storyTree.treeExpander, storyTree))
            it.add(Separator())
            it.add(TogglePreviewAction(previewPanel))
        }

        return actionManager.createActionToolbar("StoryExplorer", actionGroup, false).also {
            it.setTargetComponent(this)
            it.component.border = makeRightBorder()
        }
    }

    private fun createSplitPane(left: JComponent, right: JComponent): JBSplitter {
        return JBSplitter(
            false,
            "StoryExplorerSplitterKey",
            0.5f
        ).also {
            it.firstComponent = left
            it.secondComponent = right
        }
    }

    private fun makeRightBorder(): Border {
        return BorderFactory.createMatteBorder(0, 0, 0, 1, JBColor.border())
    }
}