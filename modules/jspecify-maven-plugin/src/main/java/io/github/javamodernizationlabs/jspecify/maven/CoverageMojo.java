package io.github.javamodernizationlabs.jspecify.maven;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.coverage.CoverageAnalyzer;
import io.github.javamodernizationlabs.jspecify.report.CoverageReportWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = "coverage", threadSafe = true, requiresProject = true)
public class CoverageMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "jspecify.outputDirectory",
            defaultValue = "${project.build.directory}/reports/jml/jspecify")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            var projectRoot = project.getBasedir().toPath();
            JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
            var summary = new CoverageAnalyzer().analyze(ProjectModel.of(projectRoot, config));
            new CoverageReportWriter().write(outputDirectory.toPath(), summary);
            getLog().info("JSpecify coverage report written to " + outputDirectory);
        } catch (Exception e) {
            throw new MojoExecutionException("JSpecify coverage failed: " + e.getMessage(), e);
        }
    }
}
