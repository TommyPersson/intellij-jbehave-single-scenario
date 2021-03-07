package se.fortnox.intellij.jbehave.utils

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

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
