package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer

class RootStoryNodeUserData(
    private val text: String
) : StoryTreeNodeUserData {

    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.Actions.GroupByTestProduction
        append(text)
    }

    override fun toSearchString(): String {
        return ""
    }
}