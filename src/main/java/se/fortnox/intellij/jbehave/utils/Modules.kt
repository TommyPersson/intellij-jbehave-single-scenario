package se.fortnox.intellij.jbehave.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

fun Module.getAllFilesByExtension(extension: String): Collection<VirtualFile> {
    return FilenameIndex.getAllFilesByExt(project, extension, GlobalSearchScope.moduleScope(this))
}

val Module.dirPath get(): String {
    return ModuleUtil.getModuleDirPath(this)
}