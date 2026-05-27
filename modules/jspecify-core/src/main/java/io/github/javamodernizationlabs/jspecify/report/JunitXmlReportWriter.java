package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.Issue;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.Severity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JunitXmlReportWriter {

    public String render(MigrationPlan plan) {
        int failures = (int) plan.issues().stream()
                .filter(i -> i.severity().atLeast(Severity.HIGH))
                .count();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<testsuite name=\"jspecify-migration\" tests=\"")
                .append(plan.issues().size())
                .append("\" failures=\"").append(failures)
                .append("\" errors=\"0\" skipped=\"0\">\n");
        for (Issue issue : plan.issues()) {
            sb.append("  <testcase classname=\"")
                    .append(escape(issue.ruleId().value()))
                    .append("\" name=\"")
                    .append(escape(issue.location().path() + ":" + issue.location().startLine()))
                    .append("\">");
            if (issue.severity().atLeast(Severity.HIGH)) {
                sb.append("<failure message=\"").append(escape(issue.message())).append("\">")
                        .append(escape(issue.fingerprint()))
                        .append("</failure>");
            } else {
                sb.append("<system-out>").append(escape(issue.message())).append("</system-out>");
            }
            sb.append("</testcase>\n");
        }
        sb.append("</testsuite>\n");
        return sb.toString();
    }

    public void write(Path output, MigrationPlan plan) throws IOException {
        Files.createDirectories(output.getParent());
        Files.writeString(output, render(plan), StandardCharsets.UTF_8);
    }

    private String escape(String raw) {
        return raw == null ? "" : raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
