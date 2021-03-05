package se.fortnox.intellij.jbehave

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.icons.AllIcons

class SingleScenarioRunLineMarkerProvider : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element.parent !is ASTWrapperPsiElement) {
            return null
        }

        if (element !is LeafPsiElement || !element.text.startsWith(JbehaveSingleScenarioAction.SCENARIO_PREFIX)) {
            return null
        }

        val scenario = JbehaveSingleScenarioAction.findScenario(element.parent.text, 0)
            ?: return null

        val storyFile = element.getContainingFile().virtualFile

        return Info(
            AllIcons.Actions.Execute,
            null,
            RunSingleScenarioAction(scenario, storyFile),
            DebugSingleScenarioAction(scenario, storyFile)
        )
    }
}