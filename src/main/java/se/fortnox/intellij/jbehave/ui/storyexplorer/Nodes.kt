package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.ColoredTreeCellRenderer
import se.fortnox.intellij.jbehave.JbehaveSingleScenarioAction
import javax.swing.tree.DefaultMutableTreeNode


interface StoryTreeNodeData {
    fun render(renderer: ColoredTreeCellRenderer)
}

class RootStoryNodeData(
    private val text: String
) : StoryTreeNodeData {

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.Actions.GroupByTestProduction
        append(text)
    }
}

class StoryNodeData private constructor(
    private var _file: PsiFile
) : StoryTreeNodeData {

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

    companion object {
        fun from(file: PsiFile) = StoryNodeData(file)
    }

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.Actions.GroupByTestProduction
        append(_text)
    }
}

class ScenarioNodeData private constructor(
    private var _element: PsiElement
) : StoryTreeNodeData {

    private var _text: String = _element.getScenarioText()

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

    private fun PsiElement.getScenarioText(): String {
        return JbehaveSingleScenarioAction.findScenario(text, 0)
    }

    companion object {
        fun from(element: PsiElement) = ScenarioNodeData(element)
    }

    override fun render(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.RunConfigurations.Junit
        append(_text)
    }
}