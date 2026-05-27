package io.github.javamodernizationlabs.jspecify.gradle;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.report.RewriteReportWriter;
import io.github.javamodernizationlabs.jspecify.rewrite.JspecifyRewriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.List;

public abstract class JspecifyRewriteHintTask extends DefaultTask {

    @Input
    public abstract Property<Boolean> getApply();

    @Input
    public abstract Property<String> getRecipe();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void run() throws IOException {
        boolean apply = getApply().getOrElse(false);
        String recipe = getRecipe().getOrElse("io.github.jml.jspecify.Migrate");
        var projectRoot = getProject().getProjectDir().toPath();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        var result = new JspecifyRewriter().rewrite(ProjectModel.of(projectRoot, config),
                List.of(recipe), apply);
        var report = getOutputDirectory().getAsFile().get().toPath().resolve("rewrite.md");
        new RewriteReportWriter().write(report, result);
        getLogger().lifecycle("JSpecify rewrite {}: {} files, {} replacements",
                apply ? "applied" : "dry run",
                result.changedFiles(),
                result.replacements());
        getLogger().lifecycle("JSpecify rewrite report written to {}", report);
    }
}
