package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.intellij.ui.treeStructure.Tree
import se.fortnox.intellij.jbehave.ui.storyexplorer.getNewSelectionUserDataAsOrNull
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryTreeNodeUserData
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener

class StoryTreeSelectionListener(
    private val storyTree: Tree
) : TreeSelectionListener {
    override fun valueChanged(e: TreeSelectionEvent) {
        val selectionUserData = e.getNewSelectionUserDataAsOrNull<StoryTreeNodeUserData>()

        storyTree.componentPopupMenu = selectionUserData?.popupMenu
    }
}
