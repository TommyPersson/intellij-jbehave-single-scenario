package se.fortnox.intellij.jbehave.utils

import com.github.kumaraman21.intellijbehave.parser.StoryFile
import com.intellij.psi.PsiClass

fun StoryFile.findStoryClass(): PsiClass? {
    return findStoryClass(virtualFile, project)
}
