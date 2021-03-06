package se.fortnox.intellij.jbehave.ui.storyexplorer.preview

import com.github.kumaraman21.intellijbehave.language.StoryFileType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.getNewSelectionUserDataAsOrNull
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryTreeNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.TreeSelectionEvent
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class ScenarioPreviewPanel(
    private val project: Project,
    storyTree: StoryTree
) {
    private val previewPanel = JPanel(BorderLayout())

    private var editor: Editor? by Delegates.observable(null, ::onEditorChanged)

    val component: JComponent = previewPanel

    init {
        storyTree.addTreeSelectionListener(::onTreeSelectionChanged)
    }

    private fun onTreeSelectionChanged(e: TreeSelectionEvent) {
        val selectionUserData = e.getNewSelectionUserDataAsOrNull<StoryTreeNodeUserData>()

        val document = selectionUserData?.castSafelyTo<StoryTreeNodeUserData>()?.previewDocument

        editor = document?.let { createEditor(it) }
    }

    private fun onEditorChanged(
        @Suppress("UNUSED_PARAMETER") property: KProperty<*>,
        old: Editor?,
        new: Editor?
    ) {
        if (old != null) {
            previewPanel.remove(old.component)
            previewPanel.isVisible = false
            old.release()
        }

        if (new != null) {
            previewPanel.add(new.component, BorderLayout.CENTER)
            previewPanel.isVisible = true
        }
    }

    private fun createEditor(document: Document): Editor {
        return EditorFactory.getInstance().createEditor(document, project, StoryFileType.STORY_FILE_TYPE, true)
    }

    private fun Editor.release() {
        EditorFactory.getInstance().releaseEditor(this)
    }
}