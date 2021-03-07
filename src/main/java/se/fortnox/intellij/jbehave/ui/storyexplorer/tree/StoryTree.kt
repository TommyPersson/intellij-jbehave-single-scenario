package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import com.intellij.util.containers.Convertor
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.RootStoryNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryTreeNodeUserData
import se.fortnox.intellij.jbehave.utils.getLastUserDataAsOrNull
import se.fortnox.intellij.jbehave.utils.getNewSelectionUserDataAsOrNull
import se.fortnox.intellij.jbehave.utils.getUserObjectAsOrNull
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class StoryTree(project: Project, toolWindow: ToolWindow) : Tree() {
    private val updater = StoryTreeUpdater(this, project, toolWindow)

    val treeExpander: com.intellij.ide.TreeExpander = TreeExpander(this)

    init {
        setCellRenderer(CellRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        addTreeSelectionListener(SelectionListener(this))
        addMouseListener(MouseListener(this))

        TreeSpeedSearch(this, SpeedSearchConvertor(), true)

        model.root.castSafelyTo<DefaultMutableTreeNode>()?.userObject = RootStoryNodeUserData("All Stories")

        updater.init()
    }

    fun refresh() {
        updater.performUpdate(reset = true)
    }

    fun getSelectionUserData(): Any? {
        return selectionModel?.selectionPath?.getLastUserDataAsOrNull<Any>()
    }


    private class SpeedSearchConvertor : Convertor<TreePath, String> {
        override fun convert(o: TreePath): String {
            val userData = o.getLastUserDataAsOrNull<StoryTreeNodeUserData>()
            return userData?.toSearchString() ?: ""
        }
    }

    private class TreeExpander(storyTree: StoryTree) : DefaultTreeExpander(storyTree) {
        override fun collapseAll(tree: JTree, keepSelectionLevel: Int) {
            // Override to change collapse behavior to non-strict.
            // That is, to keep the first level of nodes open on collapse.
            super.collapseAll(tree, false, keepSelectionLevel)
        }
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
            SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, false, selected);
        }
    }

    private class MouseListener(private val storyTree: StoryTree) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            super.mouseClicked(e)

            if (e.clickCount == 2) {
                storyTree.getClosestPathForLocation(e.x,e.y)
                    ?.getLastUserDataAsOrNull<ScenarioNodeUserData>()
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