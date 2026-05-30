package io.github.javamodernizationlabs.jspecify.config;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Immutable configuration for the JSpecify migration kit.
 *
 * <p>The canonical constructor normalizes {@code null} or empty values to the
 * documented defaults, so callers may pass {@code null} for any optional field.
 * Use {@link #defaults()} for a fully default configuration.
 *
 * @param jspecifyVersion the JSpecify annotations version to target; defaults
 *     to {@code "1.0.0"}
 * @param annotationMappings mapping from legacy nullness annotations to their
 *     JSpecify equivalents; defaults to the built-in catalog
 * @param reportFormats the report formats to produce; defaults to console,
 *     HTML, Markdown, SARIF and JSON
 * @param reportsOutputDirectory the directory reports are written to; may be
 *     relative and resolved via {@link #resolveReportsOutputDirectory(Path)}
 * @param generatedCodeExcludes glob patterns for generated sources to exclude
 *     from scanning
 * @param sourceRoots explicit source roots to scan; empty means auto-detect
 * @param markPackages packages that should be marked {@code @NullMarked}
 * @param leaveUnmarkedPackages packages that should be left unmarked
 * @param publicApiIncludes package patterns that define the public API surface;
 *     empty means include all
 * @param publicApiExcludes package patterns excluded from the public API
 *     surface
 * @param publicApiJpmsExportsOnly whether to treat only JPMS-exported packages
 *     as public API
 * @param nullawayEnabled whether NullAway integration is enabled
 * @param nullawayMode the NullAway failure mode (for example {@code "warn"} or
 *     {@code "error"}); defaults to {@code "warn"}
 * @param nullawayAnnotatedPackages packages NullAway should treat as annotated
 * @param nullawayExcludedClasses classes excluded from NullAway analysis
 * @param kotlinVerificationEnabled whether Kotlin interop verification is
 *     enabled
 * @param kotlinVerificationFailOnWarnings whether Kotlin verification warnings
 *     should fail the build
 * @param kotlinVerificationGeneratedTestsDirectory the directory for generated
 *     Kotlin verification samples
 * @param followSymlinks whether the scanner follows symbolic links
 */
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
    /**
     * Normalizes {@code null} and empty inputs to the documented defaults and
     * defensively copies the collection fields.
     */
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

    /**
     * Returns a configuration populated entirely with default values.
     *
     * @return the default configuration
     */
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

    /**
     * Resolves {@link #reportsOutputDirectory()} against the given project root.
     *
     * <p>An absolute configured directory is returned normalized; a relative one
     * is resolved against the absolute, normalized project root.
     *
     * @param projectRoot the project root to resolve a relative output
     *     directory against
     * @return the absolute, normalized reports output directory
     */
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
