package se.fortnox.intellij.jbehave.ui.storyexplorer.nodes

import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes

class ModuleNodeUserData(
    val module: Module,
) : StoryTreeNodeUserData {

    override fun renderTreeCell(renderer: ColoredTreeCellRenderer): Unit = with(renderer) {
        icon = AllIcons.Nodes.Module

        val name = module.name
        val dirPath = ModuleUtil.getModuleDirPath(module)
        val dirName = dirPath.substringAfterLast("/")

        if (name != dirName) {
            append("$dirName")
            append(" [${module.name}]", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        } else {
            append(module.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }

        append("  $dirPath", SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }

    override fun toSearchString(): String {
        return module.name
    }

    companion object {
        fun from(module: Module) = ModuleNodeUserData(module)
    }
}