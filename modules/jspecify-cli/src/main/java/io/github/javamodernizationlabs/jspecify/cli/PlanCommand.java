package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.MigrationPlanner;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.report.ConsoleReportWriter;
import io.github.javamodernizationlabs.jspecify.report.JsonReportWriter;
import io.github.javamodernizationlabs.jspecify.report.MarkdownReportWriter;
import io.github.javamodernizationlabs.jspecify.report.SarifReportWriter;
import io.github.javamodernizationlabs.jspecify.scan.AnnotationScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "plan",
        description = "Inventory legacy nullness annotations and emit a migration plan.",
        mixinStandardHelpOptions = true
)
public class PlanCommand implements Callable<Integer> {

    @Option(names = {"--project"}, description = "Project root (default: current directory)",
            defaultValue = ".")
    Path project;

    @Option(names = {"--format"}, description = "Output formats: console,json,markdown,sarif",
            split = ",", defaultValue = "console")
    List<String> formats;

    @Option(names = {"--output-dir"},
            description = "Directory for non-console reports (default: build/reports/jml/jspecify)",
            defaultValue = "build/reports/jml/jspecify")
    Path outputDir;

    @Override
    public Integer call() throws Exception {
        ProjectModel model = ProjectModel.of(project.toAbsolutePath().normalize());
        AnnotationInventory inventory = new AnnotationScanner().scan(model);
        MigrationPlan plan = new MigrationPlanner().plan(inventory);

        for (String format : formats) {
            switch (format.trim().toLowerCase()) {
                case "console" -> new ConsoleReportWriter().write(System.out, plan);
                case "json" -> new JsonReportWriter()
                        .write(outputDir.resolve("plan.json"), plan);
                case "markdown" -> new MarkdownReportWriter()
                        .write(outputDir.resolve("plan.md"), plan);
                case "sarif" -> new SarifReportWriter()
                        .write(outputDir.resolve("plan.sarif"), plan);
                default -> System.err.println("Unknown format: " + format);
            }
        }
        return 0;
    }
}
