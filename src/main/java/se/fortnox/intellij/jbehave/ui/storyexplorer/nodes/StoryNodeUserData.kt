package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.github.kumaraman21.intellijbehave.language.JBehaveIcons
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Document
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.PsiFile
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import se.fortnox.intellij.jbehave.ui.storyexplorer.directoryPathRelativeToSourceRoot
import se.fortnox.intellij.jbehave.ui.storyexplorer.pathAsPackage
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.tree.DefaultMutableTreeNode

class StoryNodeUserData private constructor(
    private var _file: PsiFile
) : StoryTreeNodeUserData {

    private val _popupMenu by lazy { createPopupMenu() }

    override val popupMenu: JBPopupMenu get() = _popupMenu

    override val previewDocument get(): Document? {
        return _file.viewProvider.document
    }

    fun update(file: PsiFile): Boolean {
        if (_file == file) {
            return false
        }

        _file = file
        return true
    }

    fun wrapInTreeNode() = DefaultMutableTreeNode(this)

    fun jumpToSource() {
        _file.navigate(true)
    }

    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = JBehaveIcons.JB

        val pkg = pathAsPackage(_file.directoryPathRelativeToSourceRoot ?: "")

        append("$pkg/")
        append(_file.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    private fun createPopupMenu(): JBPopupMenu {
        val navigateToScenarioActionItem = JBMenuItem(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) = jumpToSource()
        }).also {
            it.text = "Jump to Source"
            it.icon = AllIcons.Actions.EditSource
        }

        return JBPopupMenu().also {
            it.add(navigateToScenarioActionItem)
        }
    }

    companion object {
        fun from(file: PsiFile) = StoryNodeUserData(file)
    }
}