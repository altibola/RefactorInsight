package org.jetbrains.research.refactorinsight.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.project.Project;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.github.api.data.GHCommit;
import org.jetbrains.plugins.github.pullrequest.action.GHPRActionKeys;
import org.jetbrains.plugins.github.pullrequest.data.GHPRDataContext;
import org.jetbrains.plugins.github.pullrequest.data.GHPRDataProviderRepository;
import org.jetbrains.plugins.github.pullrequest.data.GHPRIdentifier;
import org.jetbrains.plugins.github.pullrequest.data.provider.GHPRChangesDataProvider;
import org.jetbrains.plugins.github.pullrequest.data.provider.GHPRDataProvider;
import org.jetbrains.plugins.github.pullrequest.ui.GHPRConnectedProjectViewModel;
import org.jetbrains.research.refactorinsight.RefactorInsightBundle;
import org.jetbrains.research.refactorinsight.services.WindowService;
import org.jetbrains.research.refactorinsight.pullrequests.PRVirtualFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An action that toggles the detection of refactorings in opened Pull Request.
 */
public class PRToggleRefactoringViewAction extends DumbAwareToggleAction {

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    Project project = e.getRequiredData(PlatformDataKeys.PROJECT);

    GHPRConnectedProjectViewModel connectedVm =
        e.getData(GHPRActionKeys.getPULL_REQUESTS_CONNECTED_PROJECT_VM());
    GHPRIdentifier prId = e.getData(GHPRActionKeys.getPULL_REQUEST_ID());
    if (connectedVm == null || prId == null) return;

    GHPRDataProvider dataProvider;
    try {
      GHPRDataContext dataContext = connectedVm.getDataContext();
      // The data-provider repository is internal to the GitHub plugin module (hence the
      // $intellij_vcs_github suffix), so we access it via reflection. This is fragile and
      // should be replaced once the GitHub plugin exposes a public API for retrieving PR
      // commit lists (track as technical debt).
      Method repoMethod = dataContext.getClass()
          .getMethod("getDataProviderRepository$intellij_vcs_github");
      GHPRDataProviderRepository repo = (GHPRDataProviderRepository) repoMethod.invoke(dataContext);
      dataProvider = repo.findDataProvider(prId);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    if (dataProvider == null) return;

    GHPRChangesDataProvider changesDataProvider = dataProvider.getChangesData();
    CompletableFuture<List<GHCommit>> future = new CompletableFuture<>();
    changesDataProvider.loadCommits(new Continuation<>() {
      @NotNull
      @Override
      public CoroutineContext getContext() {
        return EmptyCoroutineContext.INSTANCE;
      }

      @Override
      public void resumeWith(@NotNull Object result) {
        if (result instanceof kotlin.Result.Failure) {
          future.completeExceptionally(((kotlin.Result.Failure) result).exception);
        } else {
          @SuppressWarnings("unchecked")
          List<GHCommit> commits = (List<GHCommit>) result;
          future.complete(commits);
        }
      }
    });

    List<String> commitIds = new ArrayList<>();
    try {
      List<GHCommit> ghCommits = future.get(30, TimeUnit.SECONDS);
      for (GHCommit ghCommit : ghCommits) {
        commitIds.add(ghCommit.getOid());
      }
    } catch (InterruptedException | ExecutionException | TimeoutException ex) {
      // Timeout after 30 s; large PRs or slow connections may need more time.
      ex.printStackTrace();
      return;
    }

    PRVirtualFile prVirtualFile = new PRVirtualFile(RefactorInsightBundle.message("discovered.refactorings.in.pr"),
        null, 0, commitIds);
    ApplicationManager.getApplication()
        .invokeAndWait(() -> FileEditorManager.getInstance(project)
            .openEditor(new OpenFileDescriptor(project, prVirtualFile), true));
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return false;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(isEnabled(e));
    WindowService.getInstance(e.getProject()).update(e);
    super.update(e);
  }

  private boolean isEnabled(@NotNull AnActionEvent e) {
    return e.getProject() != null
        && e.getData(GHPRActionKeys.getPULL_REQUEST_ID()) != null;
  }
}
