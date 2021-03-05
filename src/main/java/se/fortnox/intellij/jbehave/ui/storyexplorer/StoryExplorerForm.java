package se.fortnox.intellij.jbehave.ui.storyexplorer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class StoryExplorerForm {
	private final Project    project;
	private final ToolWindow toolWindow;
	private final TreeUpdater treeUpdater;

	private Tree        storyTree;
	private JPanel      rootPanel;
	private JScrollPane scrollPane;

	public StoryExplorerForm(Project project, ToolWindow toolWindow) {
		this.project    = project;
		this.toolWindow = toolWindow;
		this.treeUpdater = new TreeUpdater(storyTree, project);

		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		DefaultMutableTreeNode root = (DefaultMutableTreeNode)storyTree.getModel().getRoot();
		root.setUserObject(new RootStoryNodeData("All Stories"));

		storyTree.setCellRenderer(new StoryTreeCellRenderer());

		PsiManager.getInstance(project).addPsiTreeChangeListener(
			new PsiTreeChangeAdapter() {
				@Override
				public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
					treeUpdater.performUpdate();
					super.childrenChanged(event);
				}
			},
			toolWindow.getDisposable()
		);
	}

	public JComponent getContent() {
		return rootPanel;
	}


}

