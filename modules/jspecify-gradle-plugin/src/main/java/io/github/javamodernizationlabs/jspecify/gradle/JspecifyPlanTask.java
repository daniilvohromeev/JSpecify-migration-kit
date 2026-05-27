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

import java.io.IOException;

public abstract class JspecifyPlanTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void run() throws IOException {
        var projectRoot = getProject().getProjectDir().toPath();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        ProjectModel model = ProjectModel.of(projectRoot, config);
        AnnotationInventory inventory = new AnnotationScanner().scan(model);
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
