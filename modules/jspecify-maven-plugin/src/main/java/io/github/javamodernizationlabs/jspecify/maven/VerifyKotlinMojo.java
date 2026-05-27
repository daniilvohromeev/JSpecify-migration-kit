package io.github.javamodernizationlabs.jspecify.maven;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.kotlin.KotlinInteropVerifier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Mojo(name = "verify-kotlin", threadSafe = true, requiresProject = true)
public class VerifyKotlinMojo extends AbstractMojo {

    @Parameter(property = "jspecify.outputDirectory",
            defaultValue = "${project.build.directory}/reports/jml/jspecify/kotlin-verification")
    private File outputDirectory;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "jspecify.kotlin.generateSamples", defaultValue = "true")
    private boolean generateSamples;

    @Parameter(property = "jspecify.kotlin.compile", defaultValue = "false")
    private boolean compile;

    @Parameter(property = "jspecify.kotlin.failOnWarnings", defaultValue = "false")
    private boolean failOnWarnings;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            var out = outputDirectory.toPath();
            Files.createDirectories(out);
            var projectRoot = project.getBasedir().toPath();
            JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
            var result = new KotlinInteropVerifier().verify(ProjectModel.of(projectRoot, config),
                    out, generateSamples, compile, List.of(project.getBuild().getOutputDirectory())
                            .stream().map(java.nio.file.Path::of).toList());
            if (failOnWarnings && !result.warnings().isEmpty()) {
                throw new MojoExecutionException("Kotlin verification warnings: "
                        + String.join("; ", result.warnings()));
            }
            getLog().info("JSpecify Kotlin verification report written to " + out);
        } catch (Exception e) {
            throw new MojoExecutionException("JSpecify Kotlin verification failed: " + e.getMessage(), e);
        }
    }
}
