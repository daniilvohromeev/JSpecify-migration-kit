package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.coverage.CoverageSummary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CoverageReportWriter {

    public String markdown(CoverageSummary summary) {
        return """
                # JSpecify Coverage

                | Metric | Value |
                |---|---:|
                | Public API elements | %d |
                | Specified public API elements | %d |
                | Public API coverage | %.1f%% |
                | Packages seen | %d |
                | @NullMarked packages | %d |
                | @NullMarked package coverage | %.1f%% |
                | Ambiguous annotations | %d |
                """.formatted(
                summary.publicApiElements(),
                summary.specifiedPublicApiElements(),
                summary.specifiedRatio() * 100.0d,
                summary.packagesSeen(),
                summary.nullMarkedPackages(),
                summary.nullMarkedPackageRatio() * 100.0d,
                summary.ambiguousAnnotations());
    }

    public String json(CoverageSummary summary) {
        return "{"
                + Json.string("publicApiElements") + ":" + Json.number(summary.publicApiElements()) + ","
                + Json.string("specifiedPublicApiElements") + ":"
                + Json.number(summary.specifiedPublicApiElements()) + ","
                + Json.string("publicApiCoverage") + ":"
                + Double.toString(summary.specifiedRatio()) + ","
                + Json.string("packagesSeen") + ":" + Json.number(summary.packagesSeen()) + ","
                + Json.string("nullMarkedPackages") + ":" + Json.number(summary.nullMarkedPackages()) + ","
                + Json.string("nullMarkedPackageCoverage") + ":"
                + Double.toString(summary.nullMarkedPackageRatio()) + ","
                + Json.string("ambiguousAnnotations") + ":"
                + Json.number(summary.ambiguousAnnotations())
                + "}";
    }

    public void write(Path outputDirectory, CoverageSummary summary) throws IOException {
        Files.createDirectories(outputDirectory);
        Files.writeString(outputDirectory.resolve("coverage.md"), markdown(summary),
                StandardCharsets.UTF_8);
        Files.writeString(outputDirectory.resolve("coverage.json"), json(summary),
                StandardCharsets.UTF_8);
    }
}
