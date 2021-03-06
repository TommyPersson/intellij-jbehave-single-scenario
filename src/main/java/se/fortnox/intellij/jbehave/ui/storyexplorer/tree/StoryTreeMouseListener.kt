package se.fortnox.intellij.jbehave.ui.storyexplorer.tree

import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.ScenarioNodeUserData
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.DefaultMutableTreeNode

class StoryTreeMouseListener(
    private val tree: Tree
) : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) {
        super.mouseClicked(e)

        if (e.clickCount == 2) {
            tree.getClosestPathForLocation(e.x,e.y)
                ?.lastPathComponent
                ?.castSafelyTo<DefaultMutableTreeNode>()
                ?.userObject
                ?.castSafelyTo<ScenarioNodeUserData>()
                ?.jumpToSource()
        }
    }
}
