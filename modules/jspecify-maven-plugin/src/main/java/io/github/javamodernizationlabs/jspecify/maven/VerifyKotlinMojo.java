package io.github.javamodernizationlabs.jspecify.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Mojo(name = "verify-kotlin", threadSafe = true, requiresProject = true)
public class VerifyKotlinMojo extends AbstractMojo {

    @Parameter(property = "jspecify.outputDirectory",
            defaultValue = "${project.build.directory}/reports/jml/jspecify/kotlin-verification")
    private File outputDirectory;

    @Parameter(property = "jspecify.kotlin.failOnWarnings", defaultValue = "false")
    private boolean failOnWarnings;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            var out = outputDirectory.toPath();
            Files.createDirectories(out);
            Files.writeString(out.resolve("kotlin-verification.md"),
                    """
                    # Kotlin interop verification

                    Fail on warnings: `%s`

                    The stable Kotlin compiler integration is tracked for the v0.2 verifier.
                    """.formatted(failOnWarnings),
                    StandardCharsets.UTF_8);
            getLog().info("JSpecify Kotlin verification report written to " + out);
        } catch (Exception e) {
            throw new MojoExecutionException("JSpecify Kotlin verification failed: " + e.getMessage(), e);
        }
    }
}
