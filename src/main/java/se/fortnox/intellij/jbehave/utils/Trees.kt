package se.fortnox.intellij.jbehave.utils

import com.intellij.util.castSafelyTo
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

inline fun <reified T : TreeNode> TreeNode.getChildAtAsOrNull(index: Int): T? {
    return this.getChildAt(index) as? T
}

inline fun <reified T> DefaultMutableTreeNode.getUserObjectAsOrNull(): T? {
    return this.userObject as? T
}

inline fun <reified T> TreePath.getLastUserDataAsOrNull(): T? {
    return lastPathComponent
        ?.castSafelyTo<DefaultMutableTreeNode>()
        ?.getUserObjectAsOrNull<T>()
}

inline fun <reified T> TreeSelectionEvent.getNewSelectionUserDataAsOrNull(): T? {
    return newLeadSelectionPath?.getLastUserDataAsOrNull<T>()
}
