package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class JspecifyVerifyKotlinTask extends DefaultTask {

    @Input
    public abstract Property<Boolean> getKotlinVerificationEnabled();

    @Input
    public abstract Property<String> getGeneratedSourceSet();

    @Input
    public abstract Property<Boolean> getFailOnWarnings();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void run() throws IOException {
        var output = getOutputDirectory().getAsFile().get().toPath();
        Files.createDirectories(output);
        String report = """
                # Kotlin interop verification

                Generated source set: `%s`
                Fail on warnings: `%s`

                This prototype creates the verification directory expected by the
                JSpecify Migration Kit workflow. Stable compilation against the
                Kotlin compiler is tracked as the v0.2 verifier milestone.
                """.formatted(getGeneratedSourceSet().getOrElse("jspecifyKotlinVerification"),
                getFailOnWarnings().getOrElse(false));
        Files.writeString(output.resolve("kotlin-verification.md"), report, StandardCharsets.UTF_8);
        getLogger().lifecycle("JSpecify Kotlin verification report written to {}", output);
    }
}
