package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.PsiElement
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.DebugSingleScenarioAction
import se.fortnox.intellij.jbehave.JbehaveSingleScenarioAction
import se.fortnox.intellij.jbehave.RunSingleScenarioAction
import se.fortnox.intellij.jbehave.ui.storyexplorer.asMenuItem
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

    override val previewDocument get(): Document {
        return EditorFactory.getInstance().createDocument(_element.text)
    }

    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.RunConfigurations.Junit
        append(_text)
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