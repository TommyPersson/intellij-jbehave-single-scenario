package se.fortnox.intellij.jbehave.utils

import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.psi.PsiClass

fun StoryFile.findJavaTestClass(): PsiClass? {
    return findJavaTestClass(virtualFile, project)
}
