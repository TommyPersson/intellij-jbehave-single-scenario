package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.openapi.editor.Document
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.ColoredTreeCellRenderer


interface StoryTreeNodeUserData {
    val popupMenu: JBPopupMenu? get() = null
    val previewDocument: Document? get() = null
    fun renderTreeCell(renderer: ColoredTreeCellRenderer)
}
