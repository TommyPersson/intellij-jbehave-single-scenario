package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.ui.ColoredTreeCellRenderer


interface StoryTreeNodeUserData {
    fun render(renderer: ColoredTreeCellRenderer)
}
