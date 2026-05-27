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

@Command(
        name = "report",
        description = "Emit JSON, Markdown and SARIF reports based on the latest scan.",
        mixinStandardHelpOptions = true
)
public class ReportCommand implements Callable<Integer> {

    @Option(names = {"--project"}, defaultValue = ".") Path project;
    @Option(names = {"--output-dir"}) Path output;

    @Override
    public Integer call() throws Exception {
        Path projectRoot = project.toAbsolutePath().normalize();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        ProjectModel model = ProjectModel.of(projectRoot, config);
        AnnotationInventory inventory = new AnnotationScanner().scan(model);
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
