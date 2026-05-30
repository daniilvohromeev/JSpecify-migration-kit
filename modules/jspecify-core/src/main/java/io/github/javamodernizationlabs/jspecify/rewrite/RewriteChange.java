package io.github.javamodernizationlabs.jspecify.rewrite;

import java.nio.file.Path;
import java.util.List;

/**
 * A single file change produced by a rewrite recipe.
 *
 * @param path the file that was (or would be) modified
 * @param description a human-readable summary of the change
 * @param replacements the number of edits applied to the file
 * @param warnings warnings emitted while computing the change; defensively copied
 */
public record RewriteChange(
        Path path,
        String description,
        int replacements,
        List<String> warnings
) {
    /**
     * Canonical constructor that defensively copies the warnings and substitutes an
     * empty list for a {@code null} argument.
     */
    public RewriteChange {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
