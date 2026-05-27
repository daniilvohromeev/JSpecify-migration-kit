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
        List<String> markPackages,
        List<String> leaveUnmarkedPackages,
        List<String> publicApiIncludes,
        List<String> publicApiExcludes,
        boolean publicApiJpmsExportsOnly,
        boolean nullawayEnabled,
        String nullawayMode,
        List<String> nullawayAnnotatedPackages,
        List<String> nullawayExcludedClasses,
        boolean kotlinVerificationEnabled,
        boolean kotlinVerificationFailOnWarnings,
        Path kotlinVerificationGeneratedTestsDirectory,
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
        markPackages = markPackages == null ? List.of() : List.copyOf(markPackages);
        leaveUnmarkedPackages = leaveUnmarkedPackages == null
                ? List.of()
                : List.copyOf(leaveUnmarkedPackages);
        publicApiIncludes = publicApiIncludes == null ? List.of() : List.copyOf(publicApiIncludes);
        publicApiExcludes = publicApiExcludes == null ? List.of() : List.copyOf(publicApiExcludes);
        nullawayMode = nullawayMode == null || nullawayMode.isBlank() ? "warn" : nullawayMode;
        nullawayAnnotatedPackages = nullawayAnnotatedPackages == null
                ? List.of()
                : List.copyOf(nullawayAnnotatedPackages);
        nullawayExcludedClasses = nullawayExcludedClasses == null
                ? List.of()
                : List.copyOf(nullawayExcludedClasses);
        kotlinVerificationGeneratedTestsDirectory = kotlinVerificationGeneratedTestsDirectory == null
                ? Path.of("build/jspecify-kotlin-verification")
                : kotlinVerificationGeneratedTestsDirectory;
    }

    public static JspecifyConfig defaults() {
        return new JspecifyConfig(
                "1.0.0",
                AnnotationCatalog.defaults().mappings(),
                List.of("console", "html", "markdown", "sarif", "json"),
                Path.of("build/reports/jml/jspecify"),
                defaultGeneratedCodeExcludes(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                false,
                "warn",
                List.of(),
                List.of(),
                false,
                false,
                Path.of("build/jspecify-kotlin-verification"),
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
