package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.MigrationPlanner;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.report.JsonReportWriter;
import io.github.javamodernizationlabs.jspecify.report.MarkdownReportWriter;
import io.github.javamodernizationlabs.jspecify.report.SarifReportWriter;
import io.github.javamodernizationlabs.jspecify.scan.AnnotationScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "report",
        description = "Emit JSON, Markdown and SARIF reports based on the latest scan.",
        mixinStandardHelpOptions = true
)
public class ReportCommand implements Callable<Integer> {

    @Option(names = {"--project"}, defaultValue = ".") Path project;
    @Option(names = {"--output-dir"}, defaultValue = "build/reports/jml/jspecify") Path output;

    @Override
    public Integer call() throws Exception {
        ProjectModel model = ProjectModel.of(project.toAbsolutePath().normalize());
        AnnotationInventory inventory = new AnnotationScanner().scan(model);
        MigrationPlan plan = new MigrationPlanner().plan(inventory);
        new JsonReportWriter().write(output.resolve("plan.json"), plan);
        new MarkdownReportWriter().write(output.resolve("plan.md"), plan);
        new SarifReportWriter().write(output.resolve("plan.sarif"), plan);
        System.out.println("Reports written to " + output);
        return 0;
    }
}
