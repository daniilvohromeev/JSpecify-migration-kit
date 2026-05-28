package io.github.javamodernizationlabs.jspecify.gradle;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.kotlin.KotlinInteropVerifier;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;
import java.nio.file.Files;

@DisableCachingByDefault(because = "Generates verification sources from local project API state.")
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
        var projectRoot = getProject().getProjectDir().toPath();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        var result = new KotlinInteropVerifier().verify(ProjectModel.of(projectRoot, config),
                output,
                getKotlinVerificationEnabled().getOrElse(false),
                getKotlinVerificationEnabled().getOrElse(false),
                java.util.List.of(projectRoot.resolve("build/classes/java/main")));
        if (getFailOnWarnings().getOrElse(false) && !result.warnings().isEmpty()) {
            throw new IOException("Kotlin verification warnings: "
                    + String.join("; ", result.warnings()));
        }
        getLogger().lifecycle("JSpecify Kotlin verification report written to {}", output);
    }
}
