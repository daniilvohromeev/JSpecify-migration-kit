package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.MigrationPlanner;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.Severity;
import io.github.javamodernizationlabs.jspecify.baseline.BaselineStore;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.report.ConsoleReportWriter;
import io.github.javamodernizationlabs.jspecify.report.HtmlReportWriter;
import io.github.javamodernizationlabs.jspecify.report.JsonReportWriter;
import io.github.javamodernizationlabs.jspecify.report.JunitXmlReportWriter;
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

    @Option(names = {"--format"}, description = "Output formats: console,json,markdown,sarif,html,junit",
            split = ",")
    List<String> formats;

    @Option(names = {"--output-dir"},
            description = "Directory for non-console reports")
    Path outputDir;

    @Option(names = {"--baseline"}, description = "Existing baseline file for CI gating")
    Path baseline;

    @Option(names = {"--baseline-write"}, description = "Write current issue fingerprints")
    Path baselineWrite;

    @Option(names = {"--fail-on"}, description = "Fail on new issues at or above severity",
            defaultValue = "high")
    String failOn;

    @Option(names = {"--allow-new-issues"}, description = "Do not fail on new issues")
    boolean allowNewIssues;

    @Override
    public Integer call() throws Exception {
        Path projectRoot = project.toAbsolutePath().normalize();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        ProjectModel model = ProjectModel.of(projectRoot, config);
        AnnotationInventory inventory = new AnnotationScanner(
                new io.github.javamodernizationlabs.jspecify.AnnotationCatalog(
                        config.annotationMappings())).scan(model);
        MigrationPlan plan = new MigrationPlanner().plan(inventory);
        List<String> effectiveFormats = formats == null || formats.isEmpty()
                ? config.reportFormats()
                : formats;
        Path effectiveOutputDir = outputDir == null
                ? config.resolveReportsOutputDirectory(projectRoot)
                : outputDir.toAbsolutePath().normalize();
        BaselineStore baselineStore = new BaselineStore();
        if (baselineWrite != null) {
            baselineStore.write(baselineWrite.toAbsolutePath().normalize(), plan.issues());
        }

        for (String format : effectiveFormats) {
            switch (format.trim().toLowerCase()) {
                case "console" -> new ConsoleReportWriter().write(System.out, plan);
                case "json" -> new JsonReportWriter()
                        .write(effectiveOutputDir.resolve("plan.json"), plan);
                case "markdown" -> new MarkdownReportWriter()
                        .write(effectiveOutputDir.resolve("plan.md"), plan);
                case "sarif" -> new SarifReportWriter()
                        .write(effectiveOutputDir.resolve("plan.sarif"), plan);
                case "html" -> new HtmlReportWriter()
                        .write(effectiveOutputDir.resolve("index.html"), plan);
                case "junit", "junit-xml" -> new JunitXmlReportWriter()
                        .write(effectiveOutputDir.resolve("TEST-jspecify-migration.xml"), plan);
                default -> System.err.println("Unknown format: " + format);
            }
        }
        if (allowNewIssues || baseline == null) {
            return 0;
        }
        Severity threshold = Severity.parse(failOn);
        boolean hasFailingNewIssues = baselineStore
                .newIssues(plan.issues(), baseline.toAbsolutePath().normalize())
                .stream()
                .anyMatch(issue -> issue.severity().atLeast(threshold));
        return hasFailingNewIssues ? 1 : 0;
    }
}
