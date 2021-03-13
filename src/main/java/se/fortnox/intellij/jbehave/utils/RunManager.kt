@file:JvmName("RunManagerUtils")

package se.fortnox.intellij.jbehave.utils

import com.intellij.compiler.options.CompileStepBeforeRun
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.psi.PsiClass


fun RunManager.createJUnitConfiguration(
    name: String,
    mainClass: PsiClass,
    extraVmParams: String = ""
): RunnerAndConfigurationSettings {
    val configuration = createConfiguration(name, JUnitConfigurationType::class.java)

    with (configuration.configuration as JUnitConfiguration) {
        setMainClass(mainClass)
        vmParameters = "${vmParameters ?: ""} $extraVmParams"
        beforeRunTasks = listOf(CompileStepBeforeRun.MakeBeforeRunTask())
    }

    return configuration
}
