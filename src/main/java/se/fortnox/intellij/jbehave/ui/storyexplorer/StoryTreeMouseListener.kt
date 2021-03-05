package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.DefaultMutableTreeNode

class StoryTreeMouseListener(
    private val tree: Tree
) : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) {
        super.mouseClicked(e)

        if (e.clickCount == 2) {
            tree.getPathForLocation(e.x,e.y)
                ?.lastPathComponent
                ?.castSafelyTo<DefaultMutableTreeNode>()
                ?.userObject
                ?.castSafelyTo<ScenarioNodeUserData>()
                ?.jumpToSource()
        }
    }
}
