package se.fortnox.intellij.jbehave.ui.storyexplorer.preview

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ex.EditorEx

interface PreviewDocument {
    val document: Document

    fun configureEditor(editor: EditorEx)
}