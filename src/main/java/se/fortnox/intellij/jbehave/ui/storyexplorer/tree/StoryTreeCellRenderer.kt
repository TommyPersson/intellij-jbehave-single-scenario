package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryTreeNodeUserData
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

internal class StoryTreeCellRenderer : ColoredTreeCellRenderer() {
    override fun customizeCellRenderer(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        val data = value.castSafelyTo<DefaultMutableTreeNode>()?.userObject?.castSafelyTo<StoryTreeNodeUserData>()
        if (data == null) {
            append("<<FIXME>>")
            return
        }

        data.render(this)
    }
}