package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.ColoredTreeCellRenderer

class ModuleNodeUserData(
    val module: Module,
) : StoryTreeNodeUserData {

    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.Nodes.Module
        append(module.name)
    }

    override fun toSearchString(): String {
        return module.name
    }

    companion object {
        fun from(module: Module) = ModuleNodeUserData(module)
    }
}