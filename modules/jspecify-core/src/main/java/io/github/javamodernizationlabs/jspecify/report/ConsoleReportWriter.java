package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.MigrationPlan;

import java.io.PrintStream;

/**
 * Renders a {@link MigrationPlan} as a human-readable summary on a text stream.
 *
 * <p>Intended for interactive console output; the layout aligns annotation
 * counts in columns and lists the recommended migration phases.
 */
public final class ConsoleReportWriter {

    /**
     * Creates a {@code ConsoleReportWriter}.
     */
    public ConsoleReportWriter() {
    }

    /**
     * Writes a plain-text summary of the migration plan to the given stream.
     *
     * @param out the stream to print the report to
     * @param plan the migration plan to render
     */
    public void write(PrintStream out, MigrationPlan plan) {
        out.println("JSpecify migration plan");
        out.println();

        if (plan.inventory().totalByAnnotation().isEmpty()) {
            out.println("No legacy nullness annotations found.");
        } else {
            out.println("Current annotations found:");
            int width = plan.inventory().totalByAnnotation().keySet().stream()
                    .mapToInt(String::length).max().orElse(40);
            for (var e : plan.inventory().totalByAnnotation().entrySet()) {
                out.printf("  %-" + width + "s %5d%n", e.getKey(), e.getValue());
            }
        }
        out.println();

        out.println("Recommended phases:");
        for (var phase : plan.phases()) {
            out.printf("  %d. %s%n", phase.order(), phase.title());
        }
        out.println();
        out.println("Estimated risk:");
        out.println("  " + plan.estimatedRisk());
        if (!plan.issues().isEmpty()) {
            out.println();
            out.printf("Issues: %d%n", plan.issues().size());
        }
    }
}
