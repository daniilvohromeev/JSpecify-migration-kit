package io.github.javamodernizationlabs.jspecify;

import java.nio.file.Path;

/**
 * A source location spanning a range within a single file.
 *
 * <p>Line and column numbers are one-based. Columns may be {@code 1} when only a
 * line is known.
 *
 * @param path the file the location refers to
 * @param startLine the one-based start line
 * @param startColumn the one-based start column
 * @param endLine the one-based end line
 * @param endColumn the one-based end column
 */
public record Location(
        Path path,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
) {
    /**
     * Creates a location pointing at a single line, with start and end columns set to one.
     *
     * @param path the file the location refers to
     * @param line the one-based line number
     * @return a location spanning the given line
     */
    public static Location of(Path path, int line) {
        return new Location(path, line, 1, line, 1);
    }

    /**
     * Returns a placeholder location with an empty path and zero coordinates,
     * used when no source location is available.
     *
     * @return an empty placeholder location
     */
    public static Location none() {
        return new Location(Path.of(""), 0, 0, 0, 0);
    }
}
