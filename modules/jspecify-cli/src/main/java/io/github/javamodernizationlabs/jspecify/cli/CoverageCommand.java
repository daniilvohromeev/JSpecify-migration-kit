package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.coverage.CoverageAnalyzer;
import io.github.javamodernizationlabs.jspecify.report.CoverageReportWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

@Command(
        name = "coverage",
        description = "Generate a public API nullness coverage report.",
        mixinStandardHelpOptions = true
)
public class CoverageCommand implements Callable<Integer> {

    @Option(names = {"--project"}, defaultValue = ".") Path project;
    @Option(names = {"--scope"}, defaultValue = "public-api") String scope;
    @Option(names = {"--format"}, split = ",") List<String> formats;
    @Option(names = {"--output-dir"}) Path output;

    @Override
    public Integer call() throws Exception {
        if (!scope.equalsIgnoreCase("public-api")) {
            System.err.println("Unsupported coverage scope: " + scope
                    + ". The JSpecify MVP currently supports public-api.");
            return 2;
        }
        Path projectRoot = project.toAbsolutePath().normalize();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        var summary = new CoverageAnalyzer().analyze(ProjectModel.of(projectRoot, config));
        Path out = output == null
                ? config.resolveReportsOutputDirectory(projectRoot)
                : output.toAbsolutePath().normalize();
        List<String> requestedFormats = formats == null || formats.isEmpty()
                ? config.reportFormats().stream()
                .filter(format -> !format.equalsIgnoreCase("console"))
                .map(format -> format.toLowerCase(Locale.ROOT))
                .toList()
                : formats;
        new CoverageReportWriter().write(out, summary, requestedFormats);
        System.out.printf("JSpecify coverage: %.1f%%%n", summary.specifiedRatio() * 100.0d);
        System.out.println("Coverage reports written to " + out);
        return 0;
    }
}
