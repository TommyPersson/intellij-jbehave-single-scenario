package se.fortnox.intellij.jbehave.ui.storyexplorer.preview

import com.github.kumaraman21.intellijbehave.language.StoryFileType
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.util.castSafelyTo
import se.fortnox.intellij.jbehave.ui.storyexplorer.nodes.StoryTreeNodeUserData
import se.fortnox.intellij.jbehave.ui.storyexplorer.tree.StoryTree
import se.fortnox.intellij.jbehave.utils.getNewSelectionUserDataAsOrNull
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

    private var editor: EditorEx? by Delegates.observable(null, ::onEditorChanged)

    var shouldShowPreview: Boolean by Delegates.observable(true, ::onShouldShowPreviewChanged)

    val component: JComponent = previewPanel

    init {
        storyTree.addTreeSelectionListener(::onTreeSelectionChanged)
    }

    private fun onTreeSelectionChanged(e: TreeSelectionEvent) {
        val selectionUserData = e.getNewSelectionUserDataAsOrNull<StoryTreeNodeUserData>()

        val document = selectionUserData?.castSafelyTo<StoryTreeNodeUserData>()?.previewDocument

        editor = document?.let { createEditor(it) }
    }

    private fun onShouldShowPreviewChanged(
        @Suppress("UNUSED_PARAMETER") property: KProperty<*>,
        old: Boolean,
        new: Boolean
    ) {
        if (previewPanel.components.any()) {
            previewPanel.isVisible = new
        } else {
            previewPanel.isVisible = false
        }
    }

    private fun onEditorChanged(
        @Suppress("UNUSED_PARAMETER") property: KProperty<*>,
        old: EditorEx?,
        new: EditorEx?
    ) {
        if (old != null) {
            previewPanel.remove(old.component)
            previewPanel.isVisible = false
            old.release()
        }

        if (new != null) {
            previewPanel.add(new.component, BorderLayout.CENTER)
            previewPanel.isVisible = shouldShowPreview
        }
    }

    private fun createEditor(previewDocument: PreviewDocument): EditorEx {
        val editor = EditorFactory.getInstance().createEditor(
            previewDocument.document,
            project,
            StoryFileType.STORY_FILE_TYPE,
            true
        ) as EditorEx

        previewDocument.configureEditor(editor)

        return editor
    }

    private fun Editor.release() {
        EditorFactory.getInstance().releaseEditor(this)
    }
}
