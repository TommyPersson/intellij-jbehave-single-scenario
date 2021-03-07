package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.github.kumaraman21.intellijbehave.language.JBehaveIcons
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.PsiFile
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import se.fortnox.intellij.jbehave.ui.storyexplorer.directoryPathRelativeToSourceRoot
import se.fortnox.intellij.jbehave.ui.storyexplorer.pathAsPackage
import se.fortnox.intellij.jbehave.ui.storyexplorer.preview.PreviewDocument
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.tree.DefaultMutableTreeNode

class StoryNodeUserData private constructor(
    private var file: PsiFile
) : StoryTreeNodeUserData {

    override val popupMenu get() = createPopupMenu()

    override val previewDocument get() = createPreviewDocument()

    fun update(file: PsiFile): Boolean {
        if (this.file == file) {
            return false
        }

        this.file = file
        return true
    }

    fun jumpToSource() {
        file.navigate(true)
    }

    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = JBehaveIcons.JB

        val pkg = getPackage()
        if (pkg.isNotEmpty()) {
            append("$pkg/")
        }

        append(file.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    override fun toSearchString(): String {
        val pkg = getPackage()

        return if (pkg.isNotEmpty()) {
            "$pkg/${file.name}"
        } else {
            file.name
        }
    }

    private fun getPackage(): String = pathAsPackage(file.directoryPathRelativeToSourceRoot ?: "")

    private fun createPreviewDocument(): PreviewDocument? {
        val document = file.viewProvider.document ?: return null

        return object : PreviewDocument {
            override val document: Document get() = document

            override fun configureEditor(editor: EditorEx) {
            }
        }
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