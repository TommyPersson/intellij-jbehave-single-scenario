package se.fortnox.intellij.jbehave.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile


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