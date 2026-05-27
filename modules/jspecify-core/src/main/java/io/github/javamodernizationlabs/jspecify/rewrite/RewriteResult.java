package io.github.javamodernizationlabs.jspecify.rewrite;

import java.util.List;

public record RewriteResult(
        boolean applied,
        List<RewriteChange> changes,
        List<String> warnings
) {
    public RewriteResult {
        changes = changes == null ? List.of() : List.copyOf(changes);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public int changedFiles() {
        return changes.size();
    }

    public int replacements() {
        return changes.stream().mapToInt(RewriteChange::replacements).sum();
    }
}
