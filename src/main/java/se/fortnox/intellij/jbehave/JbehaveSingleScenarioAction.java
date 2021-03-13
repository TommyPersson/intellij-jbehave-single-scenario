package se.fortnox.intellij.jbehave;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.fortnox.intellij.jbehave.utils.RunManagerUtils;
import se.fortnox.intellij.jbehave.utils.ScenarioUtils;
import se.fortnox.intellij.jbehave.utils.StoryFileUtils;

import javax.swing.*;

public abstract class JbehaveSingleScenarioAction extends AnAction {

	private final String actionPrefix;
	private final SingleScenarioActionData providedData;

	public JbehaveSingleScenarioAction(
		@NotNull String actionPrefix,
		@NotNull Icon icon,
		@Nullable SingleScenarioActionData data
	) {
		super(
			data != null ? data.formatActionText(actionPrefix) : "",
			data != null ? data.formatActionText(actionPrefix) : "",
			icon
		);

		this.actionPrefix = actionPrefix;
		providedData      = data;
	}

	public void actionPerformed(@NotNull AnActionEvent e) {
		SingleScenarioActionData data = getActionData(e);
		if (data == null) {
			return;
		}

		Project     project       = data.getProject();
		VirtualFile storyFile     = data.getStoryFile();
		String      scenarioTitle = data.getScenarioTitle();

		try {
			PsiClass mainClass = StoryFileUtils.findStoryClass(storyFile, project);
			if (mainClass == null) {
				throw new Exception("Unable to find class file for " + storyFile.getPresentableName());
			}

 			RunManager runManager = RunManager.getInstance(project);

			RunnerAndConfigurationSettings configuration = createConfiguration(storyFile, scenarioTitle, mainClass, runManager);
			runManager.addConfiguration(configuration);
			runManager.setSelectedConfiguration(configuration);

			executeJUnit(project, configuration.getConfiguration());
		} catch (Exception ex) {
			Messages.showMessageDialog(
				project,
				"Failed debugging scenario: " + ex.getMessage(),
				getTemplatePresentation().getText(),
				Messages.getInformationIcon()
			);
		}
	}

	protected abstract Executor getExecutorInstance();

	private void executeJUnit(Project project, RunProfile runProfile) throws ExecutionException {
		ExecutionEnvironmentBuilder.create(project, getExecutorInstance(), runProfile).buildAndExecute();
	}

	private RunnerAndConfigurationSettings createConfiguration(VirtualFile storyFile, String scenario, PsiClass mainClass, RunManager runManager) {
		String name     = storyFile.getPresentableName() + ":" + scenario;
		String filter   = ScenarioUtils.createScenarioFilterFromTitle(scenario);
		String vmParams = "-DmetaFilters=\"+scenario_title " + filter + "\"";

		return RunManagerUtils.createJUnitConfiguration(runManager, name, mainClass, vmParams);
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		SingleScenarioActionData data = getActionData(e);

		if (data != null) {
			String text = actionPrefix + " '" + ScenarioUtils.formatTrimmedTitle(data.getScenarioTitle()) + "'";
			e.getPresentation().setText(text);
			e.getPresentation().setDescription(text);
			e.getPresentation().setVisible(true);
		} else {
			e.getPresentation().setVisible(false);
		}
	}

	private @Nullable SingleScenarioActionData getActionData(AnActionEvent e) {
		if (providedData != null) {
			return providedData;
		}

		Project project = e.getProject();
		if (project == null) {
			return null;
		}

		VirtualFile storyFile = getStoryFile(e);
		if (storyFile == null) {
			return null;
		}

		String scenarioTitle = getScenarioTitle(e);
		if (scenarioTitle == null) {
			return null;
		}

		return new SingleScenarioActionData(storyFile, project, scenarioTitle);
	}

	private VirtualFile getStoryFile(AnActionEvent e) {
		VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
		if (currentFile == null) {
			return null;
		}

		if (!"story".equals(currentFile.getExtension())) {
			return null;
		}

		return currentFile;
	}

	private String getScenarioTitle(AnActionEvent e) {
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (editor == null) {
			return null;
		}

		Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
		int   start        = primaryCaret.getSelectionStart();

		String text = editor.getDocument().getText();

		return ScenarioUtils.findScenarioTitleInText(text, start);
	}
}
