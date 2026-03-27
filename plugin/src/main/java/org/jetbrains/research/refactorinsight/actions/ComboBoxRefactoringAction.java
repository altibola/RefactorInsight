package org.jetbrains.research.refactorinsight.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.vcs.log.ui.MainVcsLogUi;
import com.intellij.vcs.log.ui.VcsLogInternalDataKeys;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.refactorinsight.RefactorInsightBundle;
import org.jetbrains.research.refactorinsight.services.WindowService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ComboBoxRefactoringAction extends ComboBoxAction implements DumbAware {

    private enum ListItem {
        FILES(RefactorInsightBundle.message("ui.ChangesBrowserBase.ComboBoxAction.list.item.files")),
        REFACTORING(RefactorInsightBundle.message("ui.ChangesBrowserBase.ComboBoxAction.list.item.refactorings"));
        public final String label;
        ListItem(String label) {
            this.label = label;
        }
    }

    private DefaultActionGroup myActions;

    private ListItem currentListItem = ListItem.FILES;

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText(getText(getValue()));
        // Always keep the combobox visible in the toolbar; WindowService.update registers the
        // GitWindow lazily when the VCS Log UI is available in the DataContext.
        if (e.getProject() != null) {
            WindowService.getInstance(e.getProject()).update(e);
        }
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        // Use a DataProvider panel so that CONTEXT_COMPONENT resolves to *this* (which is always
        // visible in the toolbar), instead of traversing up to the ChangesTree's inner list which
        // may be hidden when the refactoring tree is shown in the viewport.  Without this, IntelliJ's
        // ActionManagerImpl would log "Action is not performed because target component is not showing"
        // and silently drop the "Files" / "Refactorings" switch.
        ComboBoxPanel panel = new ComboBoxPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        ComboBoxButton button = createComboBoxButton(presentation);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(UIUtil.getLabelForeground());
        button.setMargin(JBUI.emptyInsets());
        JLabel label = new JLabel(RefactorInsightBundle.message("ui.ChangesBrowserBase.ComboBoxAction.label.text"));
        label.setForeground(UIUtil.getLabelInfoForeground());
        GridBagConstraints constraints = new GridBagConstraints(
                0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, JBInsets.create(0, 0), 0, 0);
        panel.add(label, constraints);
        constraints.gridx = 1;
        panel.add(button, constraints);
        return panel;
    }

    /**
     * A JPanel that implements {@link DataProvider} to provide itself as
     * {@link PlatformCoreDataKeys#CONTEXT_COMPONENT}. This ensures that IntelliJ's
     * "target component is not showing" guard in {@code ActionManagerImpl} always finds a
     * visible (toolbar-resident) component rather than the hidden changes-browser tree list.
     */
    private static final class ComboBoxPanel extends JPanel implements DataProvider {
        ComboBoxPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public @Nullable Object getData(@NotNull String dataId) {
            if (PlatformCoreDataKeys.CONTEXT_COMPONENT.is(dataId)) {
                return this;
            }
            return null;
        }
    }

    @NotNull
    @Override
    protected DefaultActionGroup createPopupActionGroup(@NotNull JComponent button, @NotNull DataContext context) {
        if (myActions == null) {
            myActions = new DefaultActionGroup();
            for (ListItem listItem : Arrays.asList(ListItem.FILES, ListItem.REFACTORING)) {
                myActions.add(new MyAction(listItem));
            }
        }
        return myActions;
    }

    @NotNull
    private ListItem getValue() {
        return currentListItem;
    }

    private void setValue(@NotNull ListItem option) {
        if (currentListItem == option) return;
        currentListItem = option;
    }

    @Nls
    @NotNull
    private String getText(@NotNull ListItem option) {
        return option.label;
    }

    private class MyAction extends AnAction implements Toggleable, DumbAware {
        @NotNull private final ListItem myOption;

        MyAction(@NotNull ListItem option) {
            super(getText(option));
            myOption = option;
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Toggleable.setSelected(e.getPresentation(), getValue() == myOption);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            setValue(myOption);
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            MainVcsLogUi vcsLogUi = e.getRequiredData(VcsLogInternalDataKeys.MAIN_UI);
            boolean state = currentListItem == ListItem.REFACTORING;
            WindowService.getInstance(project).setSelected(vcsLogUi, state);
        }
    }
}
