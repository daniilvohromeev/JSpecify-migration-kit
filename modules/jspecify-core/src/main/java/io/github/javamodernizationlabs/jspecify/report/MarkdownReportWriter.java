package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.Issue;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MarkdownReportWriter {

    public String render(MigrationPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("# JSpecify Migration Plan\n\n");
        sb.append("- Estimated risk: **").append(plan.estimatedRisk()).append("**\n");
        sb.append("- Files scanned: ").append(plan.inventory().filesScanned()).append("\n");
        sb.append("- Annotations found: ").append(plan.inventory().totalAnnotations()).append("\n\n");

        if (!plan.inventory().totalByAnnotation().isEmpty()) {
            sb.append("## Current annotations found\n\n");
            sb.append("| Annotation | Count |\n|---|---:|\n");
            for (var e : plan.inventory().totalByAnnotation().entrySet()) {
                sb.append("| `").append(e.getKey()).append("` | ")
                        .append(e.getValue()).append(" |\n");
            }
            sb.append('\n');
        }

        sb.append("## Recommended phases\n\n");
        for (var phase : plan.phases()) {
            sb.append(phase.order()).append(". **").append(phase.title()).append("** — ")
                    .append(phase.description()).append("\n");
            for (String cmd : phase.commands()) {
                sb.append("   ```bash\n   ").append(cmd).append("\n   ```\n");
            }
        }
        sb.append('\n');

        if (!plan.issues().isEmpty()) {
            sb.append("## Issues\n\n");
            for (Issue issue : plan.issues()) {
                sb.append("- **[").append(issue.severity()).append("]** `")
                        .append(issue.ruleId()).append("` ")
                        .append(issue.message()).append(" — `")
                        .append(issue.location().path()).append(':')
                        .append(issue.location().startLine()).append("`\n");
            }
        }

        return sb.toString();
    }

    public void write(Path output, MigrationPlan plan) throws IOException {
        Files.createDirectories(output.getParent());
        Files.writeString(output, render(plan), StandardCharsets.UTF_8);
    }
}
