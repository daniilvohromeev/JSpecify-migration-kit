package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.Issue;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HtmlReportWriter {

    public String render(MigrationPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">");
        sb.append("<title>JSpecify Migration Report</title>");
        sb.append("<style>");
        sb.append("body{font-family:system-ui,sans-serif;margin:2rem;line-height:1.45}");
        sb.append("table{border-collapse:collapse;width:100%;margin:1rem 0}");
        sb.append("th,td{border:1px solid #d0d7de;padding:.45rem;text-align:left}");
        sb.append("th{background:#f6f8fa}.sev{font-weight:700}");
        sb.append("code{background:#f6f8fa;padding:.1rem .25rem}");
        sb.append("</style></head><body>");
        sb.append("<h1>JSpecify Migration Report</h1>");
        sb.append("<p><strong>Estimated risk:</strong> ")
                .append(escape(plan.estimatedRisk().name()))
                .append("</p>");
        sb.append("<p><strong>Files scanned:</strong> ")
                .append(plan.inventory().filesScanned())
                .append("</p>");
        sb.append("<p><strong>Annotations found:</strong> ")
                .append(plan.inventory().totalAnnotations())
                .append("</p>");

        sb.append("<h2>Current annotations found</h2>");
        sb.append("<table><thead><tr><th>Annotation</th><th>Count</th></tr></thead><tbody>");
        for (var entry : plan.inventory().totalByAnnotation().entrySet()) {
            sb.append("<tr><td><code>").append(escape(entry.getKey()))
                    .append("</code></td><td>").append(entry.getValue()).append("</td></tr>");
        }
        sb.append("</tbody></table>");

        sb.append("<h2>Recommended phases</h2><ol>");
        for (var phase : plan.phases()) {
            sb.append("<li><strong>").append(escape(phase.title())).append("</strong><br>")
                    .append(escape(phase.description())).append("</li>");
        }
        sb.append("</ol>");

        sb.append("<h2>Issues</h2>");
        if (plan.issues().isEmpty()) {
            sb.append("<p>No issues found.</p>");
        } else {
            sb.append("<table><thead><tr><th>Severity</th><th>Rule</th><th>Location</th><th>Message</th></tr></thead><tbody>");
            for (Issue issue : plan.issues()) {
                sb.append("<tr><td class=\"sev\">").append(escape(issue.severity().name()))
                        .append("</td><td><code>").append(escape(issue.ruleId().value()))
                        .append("</code></td><td><code>")
                        .append(escape(issue.location().path() + ":" + issue.location().startLine()))
                        .append("</code></td><td>").append(escape(issue.message()))
                        .append("</td></tr>");
            }
            sb.append("</tbody></table>");
        }
        sb.append("</body></html>");
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
                .replace("'", "&#39;");
    }
}
