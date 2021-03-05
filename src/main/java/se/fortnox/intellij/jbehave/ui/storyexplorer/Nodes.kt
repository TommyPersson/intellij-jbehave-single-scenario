package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.github.kumaraman21.intellijbehave.language.JBehaveIcons
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.DebugSingleScenarioAction
import se.fortnox.intellij.jbehave.JbehaveSingleScenarioAction
import se.fortnox.intellij.jbehave.RunSingleScenarioAction
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JPopupMenu
import javax.swing.tree.DefaultMutableTreeNode


interface StoryTreeNodeUserData {
    fun render(renderer: ColoredTreeCellRenderer)
}

class RootStoryNodeUserUserData(
    private val text: String
) : StoryTreeNodeUserData {

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.Actions.GroupByTestProduction
        append(text)
    }
}

class StoryNodeUserData private constructor(
    private var _file: PsiFile
) : StoryTreeNodeUserData {

    private var _text: String = _file.name

    fun update(file: PsiFile): Boolean {
        if (_file == file) {
            val newText = file.name
            if (newText == _text) {
                return false
            }

            _text = newText
        }

        _file = file
        return true
    }

    fun wrapInTreeNode() = DefaultMutableTreeNode(this)

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = JBehaveIcons.JB
        append(_text)
    }

    companion object {
        fun from(file: PsiFile) = StoryNodeUserData(file)
    }
}

class ScenarioNodeUserData private constructor(
    private var _element: PsiElement,
    private val tree: Tree
) : StoryTreeNodeUserData {

    private var _text: String = _element.getScenarioText()

    private val _popupMenu by lazy { createPopupMenu() }

    val popupMenu: JBPopupMenu get() = _popupMenu

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

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.RunConfigurations.Junit
        append(_text)
    }

    private fun createPopupMenu(): JBPopupMenu {
        val runActionItem = RunSingleScenarioAction(_text, _element.containingFile.virtualFile).asMenuItem(tree)
        val debugActionItem = DebugSingleScenarioAction(_text, _element.containingFile.virtualFile).asMenuItem(tree)

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

fun AnAction.asMenuItem(parent: Component): JBMenuItem {
    return JBMenuItem(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            val dataContext = DataManager.getInstance().getDataContext(parent)
            val anActionEvent = AnActionEvent.createFromAnAction(this@asMenuItem, null, "place?", dataContext)
            actionPerformed(anActionEvent)
        }
    }).also {
        it.text = templatePresentation.text
        it.icon = templatePresentation.icon
    }
}