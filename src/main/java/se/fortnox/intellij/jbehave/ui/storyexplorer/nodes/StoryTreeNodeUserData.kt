package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.ColoredTreeCellRenderer
import se.fortnox.intellij.jbehave.ui.storyexplorer.preview.PreviewDocument


interface StoryTreeNodeUserData {
    val popupMenu: JBPopupMenu? get() = null
    val previewDocument: PreviewDocument? get() = null
    fun renderTreeCell(renderer: ColoredTreeCellRenderer)
}

