package io.github.javamodernizationlabs.jspecify.gradle;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.MigrationPlanner;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.report.ConsoleReportWriter;
import io.github.javamodernizationlabs.jspecify.report.HtmlReportWriter;
import io.github.javamodernizationlabs.jspecify.report.JsonReportWriter;
import io.github.javamodernizationlabs.jspecify.report.JunitXmlReportWriter;
import io.github.javamodernizationlabs.jspecify.report.MarkdownReportWriter;
import io.github.javamodernizationlabs.jspecify.report.SarifReportWriter;
import io.github.javamodernizationlabs.jspecify.scan.AnnotationScanner;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;

/**
 * Gradle task that scans the project for legacy nullness annotations and emits a JSpecify
 * migration plan.
 *
 * <p>The task loads the JSpecify configuration, builds a project model, scans for annotations,
 * computes a migration plan and writes console, JSON, Markdown, SARIF, HTML and JUnit XML
 * reports into the configured output directory.</p>
 */
@DisableCachingByDefault(because = "Scans project sources and writes reports from local filesystem state.")
public abstract class JspecifyPlanTask extends DefaultTask {

    /**
     * Creates a {@code JspecifyPlanTask}.
     */
    public JspecifyPlanTask() {
    }

    /**
     * The directory into which the generated migration plan reports are written.
     *
     * @return a {@link DirectoryProperty} pointing at the report output directory
     */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * Scans the project and writes the JSpecify migration plan reports.
     *
     * <p>Loads the JSpecify configuration, builds the project model, scans for legacy
     * nullness annotations, plans the migration and writes the console, JSON, Markdown,
     * SARIF, HTML and JUnit XML reports.</p>
     *
     * @throws IOException if a report cannot be written to the output directory
     */
    @TaskAction
    public void run() throws IOException {
        var projectRoot = getProject().getProjectDir().toPath();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        ProjectModel model = ProjectModel.of(projectRoot, config);
        AnnotationInventory inventory = AnnotationScanner.forConfig(config).scan(model);
        MigrationPlan plan = new MigrationPlanner().plan(inventory);

        new ConsoleReportWriter().write(System.out, plan);

        var output = getOutputDirectory().getAsFile().get().toPath();
        new JsonReportWriter().write(output.resolve("plan.json"), plan);
        new MarkdownReportWriter().write(output.resolve("plan.md"), plan);
        new SarifReportWriter().write(output.resolve("plan.sarif"), plan);
        new HtmlReportWriter().write(output.resolve("index.html"), plan);
        new JunitXmlReportWriter().write(output.resolve("TEST-jspecify-migration.xml"), plan);
        getLogger().lifecycle("JSpecify reports written to {}", output);
    }
}
