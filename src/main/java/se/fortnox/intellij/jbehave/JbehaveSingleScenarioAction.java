package se.fortnox.intellij.jbehave;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.fortnox.intellij.jbehave.utils.RunManagerUtils;
import se.fortnox.intellij.jbehave.utils.StoryFileUtils;

import javax.swing.*;

public abstract class JbehaveSingleScenarioAction extends AnAction implements FileEditorProvider {

	public static final String SCENARIO_PREFIX = "Scenario:";

	private final String      providedScenarioName;
	private final VirtualFile providedStoryFile;

	public JbehaveSingleScenarioAction(
		@NotNull String text,
		@NotNull String description,
		@NotNull Icon icon,
		@Nullable String scenarioName,
		@Nullable VirtualFile storyFile
	) {
		super(text, description, icon);
		providedScenarioName = scenarioName;
		providedStoryFile    = storyFile;
	}

	public void actionPerformed(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			return;
		}

		VirtualFile storyFile = getStoryFile(project);
		if (storyFile == null) {
			return;
		}

		String scenario = getScenarioName(e);
		if (scenario == null) {
			return;
		}

		PsiClass mainClass = StoryFileUtils.findJavaTestClass(storyFile, project);

		RunManager                     runManager    = RunManager.getInstance(project);
		RunnerAndConfigurationSettings configuration = createConfiguration(storyFile, scenario, mainClass, runManager);
		runManager.addConfiguration(configuration);
		runManager.setSelectedConfiguration(configuration);

		executeJUnit(project, configuration.getConfiguration());
	}

	protected abstract Executor getExecutorInstance();

	private void executeJUnit(Project project, RunProfile runProfile) {
		try {
			ExecutionEnvironmentBuilder.create(project, getExecutorInstance(), runProfile).buildAndExecute();
		} catch (ExecutionException ex) {
			Messages.showMessageDialog(project, "Failed debugging scenario: " + ex.getMessage(),
				getTemplatePresentation().getText(), Messages.getInformationIcon());
		}
	}

	private RunnerAndConfigurationSettings createConfiguration(VirtualFile storyFile, String scenario, PsiClass mainClass, RunManager runManager) {
		String name     = storyFile.getPresentableName() + ":" + scenario;
		String filter   = ScenarioUtils.scenarioFilterFromName(scenario);
		String vmParams = "-DmetaFilters=\"+scenario_title " + filter + "\"";

		return RunManagerUtils.createJUnitConfiguration(runManager, name, mainClass, vmParams);
	}

	private VirtualFile getStoryFile(Project project) {
		if (providedStoryFile != null) {
			return providedStoryFile;
		}

		FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		VirtualFile[]     selectedFiles     = fileEditorManager.getSelectedFiles();
		if (selectedFiles.length == 0) {
			return null;
		}
		if (!"story".equals(selectedFiles[0].getExtension())) {
			return null;
		}
		return selectedFiles[0];
	}

	private String getScenarioName(AnActionEvent e) {
		if (providedScenarioName != null) {
			return providedScenarioName;
		}

		Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (editor == null) {
			return null;
		}

		Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
		if (primaryCaret == null) {
			return null;
		}
		int start = primaryCaret.getSelectionStart();

		String text = editor.getDocument().getText();
		if (text == null) {
			return null;
		}

		return findScenario(text, start);
	}

	public static String findScenario(String text, int start) {
		int scenarioStart = text.lastIndexOf(SCENARIO_PREFIX, start);
		if (scenarioStart == -1) {
			return null;
		}
		int end = text.indexOf("\n", scenarioStart);
		if (end == -1) {
			return null;
		}
		return text.substring(scenarioStart + SCENARIO_PREFIX.length(), end).trim();
	}

	@Override
	public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
		if (file.getExtension().equals("story")) {
			DefaultActionGroup editorMenu = (DefaultActionGroup)ActionManager.getInstance().getAction("EditorPopupMenu");
			editorMenu.add(this);
		}
		return false;
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		// define visibility
		e.getPresentation().setVisible(shouldShow(e));
	}

	private boolean shouldShow(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			return false;
		}

		VirtualFile storyFile = getStoryFile(project);
		if (storyFile == null) {
			return false;
		}

		String scenario = getScenarioName(e);
		return scenario != null;
	}

	@NotNull
	@Override
	public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
		return null;
	}

	@NotNull
	@Override
	public String getEditorTypeId() {
		return null;
	}

	@NotNull
	@Override
	public FileEditorPolicy getPolicy() {
		return null;
	}
}
