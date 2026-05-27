package io.github.javamodernizationlabs.jspecify;

import java.nio.file.Path;

public record Location(
        Path path,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
) {
    public static Location of(Path path, int line) {
        return new Location(path, line, 1, line, 1);
    }

    public static Location none() {
        return new Location(Path.of(""), 0, 0, 0, 0);
    }
}
