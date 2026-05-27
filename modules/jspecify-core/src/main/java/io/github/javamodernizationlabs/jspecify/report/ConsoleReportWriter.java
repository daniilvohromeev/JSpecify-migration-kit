package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.MigrationPlan;

import java.io.PrintStream;

public final class ConsoleReportWriter {

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
