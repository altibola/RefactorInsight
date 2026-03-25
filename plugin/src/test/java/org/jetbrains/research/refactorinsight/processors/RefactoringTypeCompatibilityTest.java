package org.jetbrains.research.refactorinsight.processors;

import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests that verify compatibility between the plugin's {@link RefactoringType} enum and the
 * RefactoringMiner library's {@link org.refactoringminer.api.RefactoringType} enum.
 *
 * <p>The {@link InfoFactory} calls {@code RefactoringType.valueOf(refactoring.getRefactoringType().name())}
 * at runtime, which throws {@link IllegalArgumentException} if any type name in the plugin's enum is
 * absent from (or renamed in) the RefactoringMiner enum. These tests catch such regressions early.</p>
 */
public class RefactoringTypeCompatibilityTest {

    /**
     * Every name in the plugin's {@link RefactoringType} enum must exist in
     * {@link org.refactoringminer.api.RefactoringType} so that {@link InfoFactory#create}
     * does not throw at runtime.
     */
    @Test
    public void allPluginRefactoringTypesHaveCorrespondingMinerType() {
        for (RefactoringType pluginType : RefactoringType.values()) {
            try {
                org.refactoringminer.api.RefactoringType.valueOf(pluginType.name());
            } catch (IllegalArgumentException e) {
                fail("Plugin RefactoringType." + pluginType.name()
                        + " has no corresponding entry in org.refactoringminer.api.RefactoringType."
                        + " InfoFactory.create() will throw at runtime when this refactoring type is"
                        + " returned by the miner.");
            }
        }
    }

    /**
     * {@link InfoFactory#create} must return {@code null} (and NOT throw) when RefactoringMiner
     * reports a type that the plugin does not yet know about. This ensures that upgrading
     * RefactoringMiner to a version with new refactoring types does not crash the plugin.
     */
    @Test
    public void infoFactoryReturnsNullForUnknownRefactoringType() {
        // Arrange – mock a Refactoring that carries a type name unknown to the plugin enum
        org.refactoringminer.api.RefactoringType unknownType =
                mock(org.refactoringminer.api.RefactoringType.class);
        when(unknownType.name()).thenReturn("HYPOTHETICAL_FUTURE_REFACTORING_TYPE_X");
        when(unknownType.getDisplayName()).thenReturn("Hypothetical Future Refactoring");

        Refactoring mockRefactoring = mock(Refactoring.class);
        when(mockRefactoring.getRefactoringType()).thenReturn(unknownType);

        InfoFactory factory = new InfoFactory();

        // Act & Assert – must not throw; must return null
        RefactoringInfo result = assertDoesNotThrow(
                () -> factory.create(mockRefactoring, "/some/project"),
                "InfoFactory.create() must not throw for an unknown refactoring type"
        );
        assertNull("InfoFactory.create() must return null for an unknown refactoring type", result);
    }

    // ---------------------------------------------------------------------------------------
    // Helper – assertDoesNotThrow is not in JUnit 4; provide a minimal equivalent
    // ---------------------------------------------------------------------------------------
    private static <T> T assertDoesNotThrow(ThrowingSupplier<T> supplier, String message) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            fail(message + " but threw: " + t);
            return null; // unreachable
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
}
