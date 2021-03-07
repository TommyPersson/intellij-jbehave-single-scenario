package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.getNewSelectionUserDataAsOrNull
import se.fortnox.intellij.jbehave.ui.storyexplorer.getUserObjectAsOrNull
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.RootStoryNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryTreeNodeUserData
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel

class StoryTree(project: Project, toolWindow: ToolWindow) : Tree() {
    private val updater = StoryTreeUpdater(this, project, toolWindow)

    init {
        setCellRenderer(CellRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        addTreeSelectionListener(SelectionListener(this))
        addMouseListener(MouseListener(this))

        model.root.castSafelyTo<DefaultMutableTreeNode>()?.userObject = RootStoryNodeUserData("All Stories")
        updater.init()
    }

    fun refresh() {
        updater.performUpdate(reset = true)
    }

    fun getSelectionUserData(): Any? {
        return selectionModel?.selectionPath?.lastPathComponent?.castSafelyTo<DefaultMutableTreeNode>()?.userObject
    }

    private class CellRenderer : ColoredTreeCellRenderer() {
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            val data = value.castSafelyTo<DefaultMutableTreeNode>()?.getUserObjectAsOrNull<StoryTreeNodeUserData>()
            if (data == null) {
                append("<<FIXME>>")
                return
            }

            data.renderTreeCell(this)
        }
    }

    private class MouseListener(private val storyTree: StoryTree) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            super.mouseClicked(e)

            if (e.clickCount == 2) {
                storyTree.getClosestPathForLocation(e.x,e.y)
                    ?.lastPathComponent
                    ?.castSafelyTo<DefaultMutableTreeNode>()
                    ?.getUserObjectAsOrNull<ScenarioNodeUserData>()
                    ?.jumpToSource()
            }
        }
    }

    private class SelectionListener(private val storyTree: StoryTree) : TreeSelectionListener {
        override fun valueChanged(e: TreeSelectionEvent) {
            val selectionUserData = e.getNewSelectionUserDataAsOrNull<StoryTreeNodeUserData>()

            storyTree.componentPopupMenu = selectionUserData?.popupMenu
        }
    }
}