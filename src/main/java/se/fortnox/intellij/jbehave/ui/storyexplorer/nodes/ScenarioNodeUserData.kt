package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.PsiElement
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.DebugSingleScenarioAction
import se.fortnox.intellij.jbehave.JbehaveSingleScenarioAction
import se.fortnox.intellij.jbehave.RunSingleScenarioAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.preview.PreviewDocument
import se.fortnox.intellij.jbehave.utils.asMenuItem
import se.fortnox.intellij.jbehave.utils.limitToRegion
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JPopupMenu

class ScenarioNodeUserData private constructor(
    private var element: PsiElement,
    private val tree: Tree
) : StoryTreeNodeUserData {

    private var text = element.getScenarioText()

    override val popupMenu get() = createPopupMenu()

    override val previewDocument get() = createPreviewDocument()

    val runAction get() = element.containingFile?.virtualFile?.let { RunSingleScenarioAction(text, it) }
    val debugAction get() = element.containingFile?.virtualFile?.let { DebugSingleScenarioAction(text, it) }

    fun update(element: PsiElement): Boolean {
        val newText = element.getScenarioText()

        if (this.element == element && this.text == newText) {
            return false
        }

        this.text = newText
        this.element = element

        return true
    }

    fun jumpToSource() {
        element.castSafelyTo<ASTWrapperPsiElement>()?.navigate(true)
    }

    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.RunConfigurations.Junit
        append(text)
    }

    override fun toSearchString(): String {
        return text
    }

    private fun createPreviewDocument(): PreviewDocument? {
        val document = element.containingFile?.viewProvider?.document
            ?: return null

        return object : PreviewDocument {
            override val document: Document get() = document

            override fun configureEditor(editor: EditorEx) {
                editor.limitToRegion(element)
            }
        }
    }

    private fun createPopupMenu(): JBPopupMenu? {

        val runActionItem = runAction?.asMenuItem(tree) ?: return null
        val debugActionItem = debugAction?.asMenuItem(tree) ?: return null

        val navigateToScenarioActionItem = JBMenuItem(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) = jumpToSource()
        }).also {
            it.text = "Jump to Source"
            it.icon = AllIcons.Actions.EditSource
        }

        return JBPopupMenu().also {
            it.add(navigateToScenarioActionItem)
            it.add(JPopupMenu.Separator())
            it.add(runActionItem)
            it.add(debugActionItem)
        }
    }

    private fun PsiElement.getScenarioText(): String {
        return JbehaveSingleScenarioAction.findScenario(text, 0) ?: "<Unknown>"
    }

    companion object {
        fun from(element: PsiElement, tree: Tree) = ScenarioNodeUserData(element, tree)
    }
}