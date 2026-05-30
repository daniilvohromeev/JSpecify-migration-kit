package io.github.javamodernizationlabs.jspecify.rewrite;

import java.util.List;

/**
 * The aggregate outcome of running one or more rewrite recipes.
 *
 * @param applied whether the changes were written to disk ({@code true}) or only previewed
 * @param changes the per-file changes produced; defensively copied
 * @param warnings warnings emitted across all recipes; defensively copied
 */
public record RewriteResult(
        boolean applied,
        List<RewriteChange> changes,
        List<String> warnings
) {
    /**
     * Canonical constructor that defensively copies the lists and substitutes empty
     * lists for {@code null} arguments.
     */
    public RewriteResult {
        changes = changes == null ? List.of() : List.copyOf(changes);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    /**
     * Returns the number of files that were changed.
     *
     * @return the count of changed files
     */
    public int changedFiles() {
        return changes.size();
    }

    /**
     * Returns the total number of replacements across all changed files.
     *
     * @return the sum of per-file replacement counts
     */
    public int replacements() {
        return changes.stream().mapToInt(RewriteChange::replacements).sum();
    }
}
