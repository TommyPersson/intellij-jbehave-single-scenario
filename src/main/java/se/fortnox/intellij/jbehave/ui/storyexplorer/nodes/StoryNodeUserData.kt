package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.github.kumaraman21.intellijbehave.language.JBehaveIcons
import com.intellij.psi.PsiFile
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import se.fortnox.intellij.jbehave.ui.storyexplorer.directoryPathRelativeToSourceRoot
import se.fortnox.intellij.jbehave.ui.storyexplorer.pathAsPackage
import javax.swing.tree.DefaultMutableTreeNode

class StoryNodeUserData private constructor(
    private var _file: PsiFile
) : StoryTreeNodeUserData {

    fun update(file: PsiFile): Boolean {
        if (_file == file) {
            return false
        }

        _file = file
        return true
    }

    fun wrapInTreeNode() = DefaultMutableTreeNode(this)

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = JBehaveIcons.JB

        val pkg = pathAsPackage(_file.directoryPathRelativeToSourceRoot ?: "")

        append("$pkg/")
        append(_file.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    companion object {
        fun from(file: PsiFile) = StoryNodeUserData(file)
    }
}