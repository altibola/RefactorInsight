package org.jetbrains.research.refactorinsight.processors;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import com.intellij.vcs.log.TimedVcsCommit;
import git4idea.repo.GitRepository;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.research.refactorinsight.RefactorInsightBundle;
import org.jetbrains.research.refactorinsight.data.RefactoringEntry;
import org.jetbrains.research.refactorinsight.data.RefactoringInfo;
import org.jetbrains.research.refactorinsight.services.MiningService;
import org.jetbrains.research.refactorinsight.services.SettingsState;
import org.jetbrains.research.refactorinsight.utils.TextUtils;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The CommitMiner is a Consumer of GitCommit.
 * It mines a commit and updates the refactoring map with the data retrieved for that commit.
 * Consumes a git commit, calls RefactoringMiner and detects the refactorings for a commit.
 * RefactoringMiner 3.x detects both Java and Kotlin refactorings through the same API.
 */
public class CommitMiner implements Consumer<TimedVcsCommit> {
    public static final InfoFactory INFO_FACTORY = new InfoFactory();
    private static final String progress = RefactorInsightBundle.message("progress");
    private final ExecutorService pool;
    private final Map<String, RefactoringEntry> map;
    private final Project myProject;
    private final Repository myRepository;
    private final AtomicInteger commitsDone;
    private final ProgressIndicator progressIndicator;
    private final int limit;

    /**
     * CommitMiner for mining a single commit.
     *
     * @param pool       ThreadPool to submit to.
     * @param map        Map to add mined commit data to.
     * @param repository GitRepository.
     */
    public CommitMiner(ExecutorService pool, Map<String, RefactoringEntry> map,
                       GitRepository repository,
                       AtomicInteger commitsDone, ProgressIndicator progressIndicator, int limit) {
        this.pool = pool;
        this.map = map;
        myProject = repository.getProject();
        //NB: nullable, check if initialized correctly
        myRepository = myProject.getService(MiningService.class).getRepository();
        this.commitsDone = commitsDone;
        this.progressIndicator = progressIndicator;
        this.limit = limit;
    }

    /**
     * Returns a runnable that processes only one commit using RefactoringMiner,
     * which detects refactorings in both Java and Kotlin code.
     *
     * @param commitHash       commit hash.
     * @param commitParentHash commit parent's hash.
     * @param commitTimestamp  commit timestamp.
     * @param map              the inner map that should be updated.
     * @param project          the current project.
     * @param repository       Git Repository.
     */
    public static Runnable mineAtCommit(String commitHash, String commitParentHash, long commitTimestamp,
                                        Map<String, RefactoringEntry> map,
                                        Project project, Repository repository) {
        return getRunnableToDetectRefactorings(map, commitHash, commitParentHash, commitTimestamp, repository, project);
    }

    /**
     * Creates a runnable to detect refactorings in both Java and Kotlin code via RefactoringMiner.
     * RefactoringMiner 3.x handles Kotlin natively through the same API, so no separate
     * Kotlin-specific detection step is needed.
     *
     * @param commitHash       commit hash.
     * @param commitParentHash commit parent's hash.
     * @param commitTimestamp  commit timestamp.
     * @param map              the inner map that should be updated.
     * @param project          the current project.
     * @param repository       Git Repository.
     * @return a runnable.
     */
    private static Runnable getRunnableToDetectRefactorings(Map<String, RefactoringEntry> map, String commitHash,
                                                            String commitParentHash, long commitTimestamp,
                                                            Repository repository, Project project) {
        return () -> detectRefactorings(map, commitHash, commitParentHash, commitTimestamp, repository, project);
    }

    private static void detectRefactorings(Map<String, RefactoringEntry> map, String commitHash,
                                            String commitParentHash, long commitTimestamp,
                                            Repository repository, Project project) {
        try {
            try {
                String ideaHome = com.intellij.openapi.application.PathManager.getHomePath();
                if (System.getProperty("idea.home.path") == null) {
                    System.setProperty("idea.home.path", ideaHome);
                }
            } catch (Throwable ignored) {
            }
            GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
            miner.detectAtCommit(repository, commitHash, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    createRefactoringEntry(map, commitHash, commitParentHash, commitTimestamp, project, refactorings);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createRefactoringEntry(Map<String, RefactoringEntry> map, String commitHash,
                                               String commitParentHash, long commitTimestamp,
                                               Project project, List<Refactoring> refactorings) {
        RefactoringEntry entry = new RefactoringEntry(commitHash, commitParentHash, commitTimestamp);
        if (refactorings.isEmpty()) {
            entry.setRefactorings(Collections.emptyList());
        } else {
            List<RefactoringInfo> infos = refactorings.stream()
                    .map(refactoring -> INFO_FACTORY.create(refactoring, project.getBasePath()))
                    .filter(Objects::nonNull)
                    .map(info -> info.setEntry(entry))
                    .collect(Collectors.toList());
            entry.setRefactorings(infos).combineRelated();
            entry.getRefactorings().forEach(info -> TextUtils.check(info, project));
        }
        map.merge(commitHash, entry, (old, current) -> {
            if (old.getRefactorings().isEmpty()) return current;
            if (current.getRefactorings().isEmpty()) return old;
            current.getRefactorings().addAll(old.getRefactorings());
            return current;
        });
    }

    /**
     * Mines a gitCommit.
     * Method that calls RefactoringMiner and updates the refactoring map.
     *
     * @param gitCommit to be mined
     */
    public void consume(TimedVcsCommit gitCommit) throws ProcessCanceledException {
        String commitId = gitCommit.getId().asString();

        if (!map.containsKey(commitId)) {
            pool.execute(() -> {
                if (progressIndicator.isCanceled()) {
                    cancelProgress();
                    return;
                }

                String commitParentHash =
                        gitCommit.getParents().size() == 0 ? null : gitCommit.getParents().get(0).asString();
                detectRefactorings(getRunnableToDetectRefactorings(map, commitId, commitParentHash,
                                gitCommit.getTimestamp(),
                                myRepository, myProject),
                        gitCommit.getId().asString(),
                        commitParentHash,
                        gitCommit.getTimestamp());
                incrementProgress();
            });
        } else {
            incrementProgress();
            progressIndicator.checkCanceled();
        }
    }

    private void detectRefactorings(Runnable runnable, String commitHash,
                                    String commitParentHash, long commitTimestamp) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<?> f = null;
        int timeoutSeconds = SettingsState.getInstance(myProject).miningTimeoutSeconds;
        try {
            f = service.submit(runnable);
            f.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            if (f.cancel(true)) {
                RefactoringEntry refactoringEntry =
                        RefactoringEntry.createEmptyEntry(commitHash, commitParentHash, commitTimestamp);
                refactoringEntry.setTimeout(true);
                map.put(commitHash, refactoringEntry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            service.shutdown();
        }
    }

    /**
     * Increments the progress bar with each mined commit.
     */
    private void incrementProgress() {
        final int nCommits = commitsDone.incrementAndGet();
        progressIndicator.setText(String.format(progress,
                nCommits, limit));
        progressIndicator.setFraction((float) nCommits / limit);
    }

    private void cancelProgress() {
        final int nCommits = commitsDone.incrementAndGet();
        progressIndicator.setFraction((float) nCommits / limit);
        progressIndicator.setText("Cancelling");
    }
}