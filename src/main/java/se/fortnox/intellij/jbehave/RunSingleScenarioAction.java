package se.fortnox.intellij.jbehave;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RunSingleScenarioAction extends JbehaveSingleScenarioAction {
	public RunSingleScenarioAction() {
		this(null);
	}

	public RunSingleScenarioAction(@Nullable SingleScenarioActionData data) {
		super("Run", AllIcons.Actions.Execute, data);
	}

	@Override
	protected Executor getExecutorInstance() {
		return DefaultRunExecutor.getRunExecutorInstance();
	}
}
