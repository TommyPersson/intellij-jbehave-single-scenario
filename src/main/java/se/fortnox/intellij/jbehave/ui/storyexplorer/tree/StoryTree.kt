package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.PsiElement
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import com.intellij.util.containers.Convertor
import com.intellij.util.messages.Topic
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

    sealed class NavigationRequest {
        data class ToStory(val storyFile: StoryFile) : NavigationRequest()
        data class ToScenario(val file: StoryFile, val scenarioElement: PsiElement) : NavigationRequest()
    }

    interface NavigationNotifier {
        companion object {
            val MESSAGE_BUS_TOPIC = Topic.create("story_tree_navigation_requests", NavigationNotifier::class.java)
        }

        fun requestNavigation(navigationRequest: NavigationRequest)
    }

    private val updater = StoryTreeUpdater(this, project, toolWindow)

    val treeExpander: com.intellij.ide.TreeExpander = TreeExpander()

    init {
        setCellRenderer(CellRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        addTreeSelectionListener(SelectionListener())
        addMouseListener(MouseListener())

        TreeSpeedSearch(this, SpeedSearchConvertor(), true)

        model.root.castSafelyTo<DefaultMutableTreeNode>()?.userObject = RootStoryNodeUserData("All Stories")

        isRootVisible = false

        project.messageBus.connect(toolWindow.disposable)
            .subscribe(NavigationNotifier.MESSAGE_BUS_TOPIC, NavigationListener())
    }

    fun refresh() {
        updater.queueFullTreeReset()
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

    private inner class TreeExpander : DefaultTreeExpander(this) {
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

    private inner class MouseListener : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            super.mouseClicked(e)

            if (e.clickCount == 2) {
                getClosestPathForLocation(e.x, e.y)
                    ?.getLastUserDataAsOrNull<ScenarioNodeUserData>()
                    ?.jumpToSource()
            }
        }
    }

    private inner class SelectionListener : TreeSelectionListener {
        override fun valueChanged(e: TreeSelectionEvent) {
            val selectionUserData = e.getNewSelectionUserDataAsOrNull<StoryTreeNodeUserData>()

            componentPopupMenu = selectionUserData?.popupMenu
        }
    }

    private inner class NavigationListener : NavigationNotifier {
        override fun requestNavigation(navigationRequest: NavigationRequest) {
            val node = when (navigationRequest) {
                is NavigationRequest.ToStory -> updater.findStoryNode(navigationRequest.storyFile)
                is NavigationRequest.ToScenario -> updater.findScenarioNode(navigationRequest.scenarioElement)
            } ?: return

            selectionPath = TreePath(node.path)
            scrollPathToVisible(selectionPath)
            requestFocus()
        }
    }
}