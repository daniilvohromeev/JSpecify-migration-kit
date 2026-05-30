package io.github.javamodernizationlabs.jspecify.gradle;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.coverage.CoverageAnalyzer;
import io.github.javamodernizationlabs.jspecify.report.CoverageReportWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;

/**
 * Gradle task that generates a public API nullness coverage report.
 *
 * <p>The task loads the JSpecify configuration, builds a project model, analyses how much of
 * the public API has been annotated for nullness and writes a coverage report into the
 * configured output directory.</p>
 */
@DisableCachingByDefault(because = "Scans project sources and writes reports from local filesystem state.")
public abstract class JspecifyCoverageTask extends DefaultTask {

    /**
     * Creates a {@code JspecifyCoverageTask}.
     */
    public JspecifyCoverageTask() {
    }

    /**
     * The directory into which the coverage report is written.
     *
     * @return a {@link DirectoryProperty} pointing at the report output directory
     */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * Analyses the project and writes the public API nullness coverage report.
     *
     * <p>Loads the JSpecify configuration, builds the project model, computes the coverage
     * summary and writes it to the output directory.</p>
     *
     * @throws IOException if the coverage report cannot be written to the output directory
     */
    @TaskAction
    public void run() throws IOException {
        var projectRoot = getProject().getProjectDir().toPath();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        ProjectModel model = ProjectModel.of(projectRoot, config);
        var summary = new CoverageAnalyzer().analyze(model);
        var output = getOutputDirectory().getAsFile().get().toPath();
        new CoverageReportWriter().write(output, summary);
        getLogger().lifecycle("JSpecify coverage report written to {}", output);
    }
}
