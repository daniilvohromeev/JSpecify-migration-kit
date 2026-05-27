package io.github.javamodernizationlabs.jspecify.maven;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.report.RewriteReportWriter;
import io.github.javamodernizationlabs.jspecify.rewrite.JspecifyRewriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

@Mojo(name = "rewrite-hint", threadSafe = true, requiresProject = true)
public class RewriteHintMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "jspecify.outputDirectory",
            defaultValue = "${project.build.directory}/reports/jml/jspecify")
    private File outputDirectory;

    @Parameter(property = "jspecify.recipe",
            defaultValue = "io.github.jml.jspecify.Migrate")
    private String recipe = "io.github.jml.jspecify.Migrate";

    @Parameter(property = "jspecify.apply", defaultValue = "false")
    private boolean apply;

    public RewriteHintMojo() {
    }

    protected RewriteHintMojo(boolean apply) {
        this.apply = apply;
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            var projectRoot = project.getBasedir().toPath();
            JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
            var result = new JspecifyRewriter().rewrite(ProjectModel.of(projectRoot, config),
                    List.of(recipe), apply);
            var report = outputDirectory.toPath().resolve("rewrite.md");
            new RewriteReportWriter().write(report, result);
            getLog().info("JSpecify rewrite " + (apply ? "applied" : "dry run")
                    + ": " + result.changedFiles() + " files, "
                    + result.replacements() + " replacements");
            getLog().info("JSpecify rewrite report written to " + report);
        } catch (Exception e) {
            throw new MojoExecutionException("JSpecify rewrite failed: " + e.getMessage(), e);
        }
    }
}
