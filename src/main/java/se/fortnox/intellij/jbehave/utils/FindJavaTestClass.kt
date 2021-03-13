@file:JvmName("StoryFileUtils")

package se.fortnox.intellij.jbehave.utils

import com.google.common.base.CaseFormat
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass

fun findJavaTestClass(storyFile: VirtualFile, project: Project): PsiClass? {
    val pkg: String = storyFile.parent
        .path
        .substring(storyFile.path.indexOf("src/test/resources") + "src/test/resources".length)
        .replace("/", ".")
        .replace("^\\.".toRegex(), "")

    val className = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, storyFile.name.replace(".story", ""))

    val qualifiedName = if (pkg.isEmpty()) className else "$pkg.$className"

    return project.findJavaClass(qualifiedName)
}