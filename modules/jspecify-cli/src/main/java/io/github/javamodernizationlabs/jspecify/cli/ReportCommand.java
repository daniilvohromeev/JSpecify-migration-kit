package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.MigrationPlanner;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.report.HtmlReportWriter;
import io.github.javamodernizationlabs.jspecify.report.JsonReportWriter;
import io.github.javamodernizationlabs.jspecify.report.JunitXmlReportWriter;
import io.github.javamodernizationlabs.jspecify.report.MarkdownReportWriter;
import io.github.javamodernizationlabs.jspecify.report.SarifReportWriter;
import io.github.javamodernizationlabs.jspecify.scan.AnnotationScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * The {@code report} command, which scans a project and writes the full set of migration
 * reports.
 *
 * <p>It scans the project rooted at {@code --project}, builds a migration plan and writes the
 * JSON, Markdown, SARIF, HTML and JUnit XML reports into the directory selected by
 * {@code --output-dir}. Unlike {@code plan}, this command always emits every report format
 * and performs no baseline gating.</p>
 */
@Command(
        name = "report",
        description = "Emit JSON, Markdown and SARIF reports based on the latest scan.",
        mixinStandardHelpOptions = true
)
public class ReportCommand implements Callable<Integer> {

    /**
     * Creates a {@code ReportCommand}.
     */
    public ReportCommand() {
    }

    @Option(names = {"--project"}, defaultValue = ".") Path project;
    @Option(names = {"--output-dir"}) Path output;

    /**
     * Scans the project, builds the migration plan and writes all report formats.
     *
     * @return the process exit code; always {@code 0} once the reports have been written
     * @throws Exception if scanning, planning or writing any report fails
     */
    @Override
    public Integer call() throws Exception {
        Path projectRoot = project.toAbsolutePath().normalize();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        ProjectModel model = ProjectModel.of(projectRoot, config);
        AnnotationInventory inventory = AnnotationScanner.forConfig(config).scan(model);
        MigrationPlan plan = new MigrationPlanner().plan(inventory);
        Path out = output == null
                ? config.resolveReportsOutputDirectory(projectRoot)
                : output.toAbsolutePath().normalize();
        new JsonReportWriter().write(out.resolve("plan.json"), plan);
        new MarkdownReportWriter().write(out.resolve("plan.md"), plan);
        new SarifReportWriter().write(out.resolve("plan.sarif"), plan);
        new HtmlReportWriter().write(out.resolve("index.html"), plan);
        new JunitXmlReportWriter().write(out.resolve("TEST-jspecify-migration.xml"), plan);
        System.out.println("Reports written to " + out);
        return 0;
    }
}
