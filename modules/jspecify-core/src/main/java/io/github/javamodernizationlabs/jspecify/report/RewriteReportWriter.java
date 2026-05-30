package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.rewrite.RewriteResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Renders a {@link RewriteResult} as a Markdown summary.
 *
 * <p>The report lists whether changes were applied, the per-file changes with
 * their replacement counts, and any warnings produced during the rewrite.
 */
public final class RewriteReportWriter {

    /**
     * Creates a {@code RewriteReportWriter}.
     */
    public RewriteReportWriter() {
    }

    /**
     * Renders the rewrite result as a Markdown string.
     *
     * @param result the rewrite result to render
     * @return the rewrite report as Markdown text
     */
    public String markdown(RewriteResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("# JSpecify Rewrite Report\n\n");
        sb.append("- Applied: `").append(result.applied()).append("`\n");
        sb.append("- Changed files: ").append(result.changedFiles()).append("\n");
        sb.append("- Replacements: ").append(result.replacements()).append("\n\n");
        if (!result.changes().isEmpty()) {
            sb.append("## Changes\n\n");
            for (var change : result.changes()) {
                sb.append("- `").append(change.path()).append("`: ")
                        .append(change.description())
                        .append(" (").append(change.replacements()).append(" replacements)\n");
                for (String warning : change.warnings()) {
                    sb.append("  - Warning: ").append(warning).append("\n");
                }
            }
            sb.append('\n');
        }
        if (!result.warnings().isEmpty()) {
            sb.append("## Warnings\n\n");
            for (String warning : result.warnings()) {
                sb.append("- ").append(warning).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Renders the result and writes it to the given file, creating parent
     * directories as needed.
     *
     * @param output the file path to write the Markdown report to
     * @param result the rewrite result to render
     * @throws IOException if the parent directories or file cannot be written
     */
    public void write(Path output, RewriteResult result) throws IOException {
        Files.createDirectories(output.getParent());
        Files.writeString(output, markdown(result), StandardCharsets.UTF_8);
    }
}
