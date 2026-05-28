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

@DisableCachingByDefault(because = "Scans project sources and writes reports from local filesystem state.")
public abstract class JspecifyCoverageTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

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
