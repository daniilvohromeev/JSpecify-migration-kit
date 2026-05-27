package io.github.javamodernizationlabs.jspecify.config;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record JspecifyConfig(
        String jspecifyVersion,
        Map<String, String> annotationMappings,
        List<String> reportFormats,
        Path reportsOutputDirectory,
        List<String> generatedCodeExcludes,
        List<Path> sourceRoots,
        boolean followSymlinks
) {
    public JspecifyConfig {
        jspecifyVersion = jspecifyVersion == null ? "1.0.0" : jspecifyVersion;
        annotationMappings = annotationMappings == null
                ? AnnotationCatalog.defaults().mappings()
                : Map.copyOf(annotationMappings);
        reportFormats = reportFormats == null || reportFormats.isEmpty()
                ? List.of("console", "html", "markdown", "sarif", "json")
                : List.copyOf(reportFormats);
        reportsOutputDirectory = reportsOutputDirectory == null
                ? Path.of("build/reports/jml/jspecify")
                : reportsOutputDirectory;
        generatedCodeExcludes = generatedCodeExcludes == null
                ? defaultGeneratedCodeExcludes()
                : List.copyOf(generatedCodeExcludes);
        sourceRoots = sourceRoots == null ? List.of() : List.copyOf(sourceRoots);
    }

    public static JspecifyConfig defaults() {
        return new JspecifyConfig(
                "1.0.0",
                AnnotationCatalog.defaults().mappings(),
                List.of("console", "html", "markdown", "sarif", "json"),
                Path.of("build/reports/jml/jspecify"),
                defaultGeneratedCodeExcludes(),
                List.of(),
                false);
    }

    public Path resolveReportsOutputDirectory(Path projectRoot) {
        if (reportsOutputDirectory.isAbsolute()) {
            return reportsOutputDirectory.normalize();
        }
        return projectRoot.toAbsolutePath().normalize()
                .resolve(reportsOutputDirectory)
                .normalize();
    }

    private static List<String> defaultGeneratedCodeExcludes() {
        return List.of(
                "**/generated/**",
                "**/target/generated-sources/**",
                "**/build/generated/**");
    }
}
