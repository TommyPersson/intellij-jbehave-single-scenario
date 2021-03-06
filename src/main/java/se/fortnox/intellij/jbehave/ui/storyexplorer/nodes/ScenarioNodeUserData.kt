package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.DebugSingleScenarioAction
import se.fortnox.intellij.jbehave.JbehaveSingleScenarioAction
import se.fortnox.intellij.jbehave.RunSingleScenarioAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.asMenuItem
import se.fortnox.intellij.jbehave.ui.storyexplorer.preview.PreviewDocument
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JPopupMenu
import javax.swing.tree.DefaultMutableTreeNode

class ScenarioNodeUserData private constructor(
    private var _element: PsiElement,
    private val tree: Tree
) : StoryTreeNodeUserData {

    private var _text: String = _element.getScenarioText()

    private val _popupMenu by lazy { createPopupMenu() }

    override val popupMenu: JBPopupMenu get() = _popupMenu


    // TODO memoize on _element
    val runAction by lazy { RunSingleScenarioAction(_text, _element.containingFile.virtualFile) }
    val debugAction by lazy { DebugSingleScenarioAction(_text, _element.containingFile.virtualFile) }

    fun update(element: PsiElement): Boolean {
        if (_element == element) {
            val newText = element.getScenarioText()
            if (newText == _text) {
                return false
            }

            _text = newText
        }

        _element = element
        return true
    }

    fun wrapInTreeNode() = DefaultMutableTreeNode(this)

    fun jumpToSource() {
        _element.castSafelyTo<ASTWrapperPsiElement>()?.navigate(true)
    }

    override val previewDocument get(): PreviewDocument? = createPreviewDocument()


    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.RunConfigurations.Junit
        append(_text)
    }

    private fun createPreviewDocument(): PreviewDocument? {
        val document = _element.containingFile.viewProvider.document
            ?: return null

        return object : PreviewDocument {
            override val document: Document get() = document

            override fun configureEditor(editor: EditorEx) {
                editor.limitToRegion(_element)
            }

            private fun EditorEx.limitToRegion(region: PsiElement) {
                with (foldingModel) {
                    runBatchFoldingOperation {
                        if (region.startOffset != 0) {
                            addInvisibleFold(0, region.startOffset)
                        }

                        if (region.endOffset != region.containingFile.endOffset) {
                            addInvisibleFold(region.endOffset, region.containingFile.endOffset)
                        }
                    }
                }
            }

            private fun FoldingModelEx.addInvisibleFold(start: Int, end: Int) {
                createFoldRegion(start, end, "", null, true)
            }
        }
    }

    private fun createPopupMenu(): JBPopupMenu {

        val runActionItem = runAction.asMenuItem(tree)
        val debugActionItem = debugAction.asMenuItem(tree)

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
        return JbehaveSingleScenarioAction.findScenario(text, 0)
    }

    companion object {
        fun from(element: PsiElement, tree: Tree) = ScenarioNodeUserData(element, tree)
    }
}
