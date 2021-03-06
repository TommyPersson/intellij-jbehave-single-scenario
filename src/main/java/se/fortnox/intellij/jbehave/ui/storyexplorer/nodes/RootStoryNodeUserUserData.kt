package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer

class RootStoryNodeUserUserData(
    private val text: String
) : StoryTreeNodeUserData {

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.Actions.GroupByTestProduction
        append(text)
    }
}