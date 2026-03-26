package org.jetbrains.research.refactorinsight.processors;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.refactorinsight.data.JavaRefactoringHandler;
import org.jetbrains.research.refactorinsight.data.RefactoringInfo;
import org.refactoringminer.api.Refactoring;

/**
 * Creates {@link RefactoringInfo} objects for refactorings provided by RefactoringMiner.
 * RefactoringMiner 3.x detects both Java and Kotlin refactorings natively through the same
 * {@link Refactoring} interface, so no separate Kotlin handler path is required.
 */
public class InfoFactory {
    /**
     * Creates a relevant {@link RefactoringInfo} instance for a given {@link Refactoring}.
     * Returns {@code null} when no handler is registered for the type (the refactoring is
     * intentionally not visualised) or when the type is unknown to this version of the plugin
     * (graceful forward-compatibility with newer RefactoringMiner releases).
     *
     * @param refactoring to be analysed.
     * @param projectPath root path of the project being mined.
     * @return resulting RefactoringInfo, or {@code null} if the type is unsupported / unknown.
     */
    @Nullable
    public RefactoringInfo create(Refactoring refactoring, String projectPath) {
        JavaRefactoringHandler handler;
        try {
            handler = RefactoringType.valueOf(refactoring.getRefactoringType().name()).getJavaHandler();
        } catch (IllegalArgumentException e) {
            // RefactoringMiner returned a type not yet known to this version of the plugin.
            // Skip it gracefully rather than crashing the whole mining run.
            return null;
        }
        return handler == null ? null : handler.handle(refactoring, projectPath)
                .setProjectPath(projectPath)
                .setType(refactoring.getRefactoringType().getDisplayName());
    }
}

