package io.github.javamodernizationlabs.jspecify.maven;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.MigrationPlanner;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.report.JsonReportWriter;
import io.github.javamodernizationlabs.jspecify.report.MarkdownReportWriter;
import io.github.javamodernizationlabs.jspecify.report.SarifReportWriter;
import io.github.javamodernizationlabs.jspecify.scan.AnnotationScanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = "report", threadSafe = true, requiresProject = true)
public class ReportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "jspecify.outputDirectory",
            defaultValue = "${project.build.directory}/reports/jml/jspecify")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            ProjectModel model = ProjectModel.of(project.getBasedir().toPath());
            AnnotationInventory inventory = new AnnotationScanner().scan(model);
            MigrationPlan plan = new MigrationPlanner().plan(inventory);

            var out = outputDirectory.toPath();
            new JsonReportWriter().write(out.resolve("plan.json"), plan);
            new MarkdownReportWriter().write(out.resolve("plan.md"), plan);
            new SarifReportWriter().write(out.resolve("plan.sarif"), plan);
            getLog().info("JSpecify reports written to " + out);
        } catch (Exception e) {
            throw new MojoExecutionException("JSpecify report failed: " + e.getMessage(), e);
        }
    }
}
