package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.util.castSafelyTo
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode


inline fun <reified T : TreeNode> TreeNode.getChildAtAsOrNull(index: Int): T? {
    return this.getChildAt(index) as? T
}

inline fun <reified T> DefaultMutableTreeNode.getUserObjectAsOrNull(): T? {
    return this.userObject as? T
}

inline fun <reified T> TreeSelectionEvent.getNewSelectionUserDataAsOrNull(): T? {
    return newLeadSelectionPath
        ?.lastPathComponent
        ?.castSafelyTo<DefaultMutableTreeNode>()
        ?.getUserObjectAsOrNull<T>()
}

fun Project.findPsiFile(virtualFile: VirtualFile): PsiFile? {
    return PsiManager.getInstance(this).findFile(virtualFile)
}

fun Project.getAllFilesByExtension(extension: String): Collection<VirtualFile> {
    return FilenameIndex.getAllFilesByExt(this, extension)
}

val PsiFile.containingModule get(): Module? {
    return ModuleUtil.findModuleForPsiElement(this)
}

val PsiFile.containingContentRoot get(): VirtualFile? {
    return virtualFile.findContainingContentRoot(project)
}

fun VirtualFile.findContainingContentRoot(project: Project): VirtualFile? {
    return ProjectRootManager.getInstance(project).fileIndex.getSourceRootForFile(this)
}

val PsiFile.directoryPathRelativeToSourceRoot get(): String? {
    return virtualFile.directoryPathRelativeToSourceRoot(project)
}

fun VirtualFile.directoryPathRelativeToSourceRoot(project: Project): String? {
    val sourceRootPath = this.findContainingContentRoot(project)?.canonicalPath
        ?: return null

    val directoryPath = this.parent?.canonicalPath
        ?: return null

    return directoryPath.replace(sourceRootPath, "")
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

inline fun <reified T : Component> AnActionEvent.getContextComponent(): T? {
    return dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT) as? T
}

fun pathAsPackage(path: String): String {
    return path.trim('/').replace('/', '.')
}