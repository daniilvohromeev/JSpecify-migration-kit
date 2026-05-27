package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.report.RewriteReportWriter;
import io.github.javamodernizationlabs.jspecify.rewrite.JspecifyRewriter;
import io.github.javamodernizationlabs.jspecify.rewrite.RewriteResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "rewrite",
        description = "Apply OpenRewrite recipes via the OpenRewrite Maven/Gradle plugin.",
        mixinStandardHelpOptions = true
)
public class RewriteCommand implements Callable<Integer> {

    @Option(names = {"--project"}, description = "Project root (default: current directory)",
            defaultValue = ".")
    Path project;

    @Option(names = {"--recipe"},
            description = "Recipe ids, e.g. add-dependency,convert-known-annotations.",
            split = ",", required = true)
    List<String> recipes;

    @Option(names = {"--dry-run"}, description = "Only print the planned changes.")
    boolean dryRun;

    @Option(names = {"--apply"}, description = "Apply the changes in-place.")
    boolean apply;

    @Option(names = {"--output-dir"}) Path outputDir;

    @Override
    public Integer call() throws Exception {
        if (apply == dryRun) {
            System.err.println("Choose exactly one of --dry-run or --apply.");
            return 2;
        }
        Path projectRoot = project.toAbsolutePath().normalize();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        ProjectModel model = ProjectModel.of(projectRoot, config);
        RewriteResult result = new JspecifyRewriter(new AnnotationCatalog(
                config.annotationMappings())).rewrite(model, recipes, apply);
        Path out = outputDir == null
                ? config.resolveReportsOutputDirectory(projectRoot)
                : outputDir.toAbsolutePath().normalize();
        new RewriteReportWriter().write(out.resolve("rewrite.md"), result);
        System.out.printf("Rewrite %s: %d files, %d replacements%n",
                apply ? "applied" : "dry run",
                result.changedFiles(),
                result.replacements());
        if (!result.warnings().isEmpty()) {
            System.out.printf("Warnings: %d%n", result.warnings().size());
        }
        System.out.println("Rewrite report written to " + out.resolve("rewrite.md"));
        return 0;
    }
}
