package se.fortnox.intellij.jbehave;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugSingleScenarioAction extends JbehaveSingleScenarioAction {

	public DebugSingleScenarioAction() {
		this(null);
	}

	public DebugSingleScenarioAction(@Nullable SingleScenarioActionData data) {
		super("Debug", AllIcons.Actions.StartDebugger, data);
	}

	@Override
	protected Executor getExecutorInstance() {
		return DefaultDebugExecutor.getDebugExecutorInstance();
	}
}
