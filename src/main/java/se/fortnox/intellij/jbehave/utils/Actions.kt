package se.fortnox.intellij.jbehave.utils

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.JBMenuItem
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

fun AnAction.asMenuItem(parent: Component): JBMenuItem {
    return JBMenuItem(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            val dataContext = DataManager.getInstance().getDataContext(parent)
            val anActionEvent = AnActionEvent.createFromAnAction(this@asMenuItem, null, "place?", dataContext)
            actionPerformed(anActionEvent)
        }
    }).also {
        it.text = templatePresentation.text
        it.icon = templatePresentation.icon
    }
}

inline fun <reified T : Component> AnActionEvent.getContextComponent(): T? {
    return dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT) as? T
}