package io.github.javamodernizationlabs.jspecify.rewrite;

import java.nio.file.Path;
import java.util.List;

public record RewriteChange(
        Path path,
        String description,
        int replacements,
        List<String> warnings
) {
    public RewriteChange {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
