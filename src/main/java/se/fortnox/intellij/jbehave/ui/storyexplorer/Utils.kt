package se.fortnox.intellij.jbehave.ui.storyexplorer

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.castSafelyTo
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath


inline fun <reified T : TreeNode> TreeNode.getChildAtAsOrNull(index: Int): T? {
    return this.getChildAt(index) as? T
}

inline fun <reified T> DefaultMutableTreeNode.getUserObjectAsOrNull(): T? {
    return this.userObject as? T
}

inline fun <reified T> TreePath.getLastUserDataAsOrNull(): T? {
    return lastPathComponent
        ?.castSafelyTo<DefaultMutableTreeNode>()
        ?.getUserObjectAsOrNull<T>()
}

inline fun <reified T> TreeSelectionEvent.getNewSelectionUserDataAsOrNull(): T? {
    return newLeadSelectionPath?.getLastUserDataAsOrNull<T>()
}

fun Project.findPsiFile(virtualFile: VirtualFile): PsiFile? {
    return PsiManager.getInstance(this).findFile(virtualFile)
}

fun Project.getAllFilesByExtension(extension: String): Collection<VirtualFile> {
    return FilenameIndex.getAllFilesByExt(this, extension)
}

val Project.modules get(): List<Module> {
    return ModuleManager.getInstance(this).modules.toList()
}

fun Module.getAllFilesByExtension(extension: String): Collection<VirtualFile> {
    return FilenameIndex.getAllFilesByExt(project, extension, GlobalSearchScope.moduleScope(this))
}

val Module.dirPath get(): String {
    return ModuleUtil.getModuleDirPath(this)
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

fun EditorEx.limitToRegion(region: PsiElement) {
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

fun FoldingModelEx.addInvisibleFold(start: Int, end: Int) {
    createFoldRegion(start, end, "", null, true)
}
