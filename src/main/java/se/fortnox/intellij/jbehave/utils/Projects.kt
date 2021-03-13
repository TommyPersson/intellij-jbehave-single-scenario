package se.fortnox.intellij.jbehave.utils

import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope

fun Project.findPsiFile(virtualFile: VirtualFile): PsiFile? {
    return PsiManager.getInstance(this).findFile(virtualFile)
}

val Project.modules get(): List<Module> {
    return ModuleManager.getInstance(this).modules.toList()
}

fun Project.addPsiTreeChangeListener(listener: PsiTreeChangeListener, parentDisposable: Disposable) {
    PsiManager.getInstance(this).addPsiTreeChangeListener(listener, parentDisposable)
}

fun Project.findJavaClass(qualifiedName: String): PsiClass? {
    val scope = GlobalSearchScope.projectScope(this)
    val facade = JavaPsiFacade.getInstance(this)
    return facade.findClass(qualifiedName, scope)
}