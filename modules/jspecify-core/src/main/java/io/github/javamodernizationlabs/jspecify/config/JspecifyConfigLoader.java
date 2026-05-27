package io.github.javamodernizationlabs.jspecify.config;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Small YAML reader for the documented jml.yml/jspecify.yml surface.
 *
 * <p>It deliberately supports only the simple maps and scalar lists used by the
 * published config examples. Unknown keys are ignored so newer config files do
 * not break older binaries.
 */
public final class JspecifyConfigLoader {

    private JspecifyConfigLoader() {}

    public static JspecifyConfig load(Path projectRoot) throws IOException {
        Path root = projectRoot.toAbsolutePath().normalize();
        ConfigBuilder builder = new ConfigBuilder();
        loadIfExists(root.resolve("jml.yml"), builder);
        loadIfExists(root.resolve("jspecify.yml"), builder);
        return builder.build();
    }

    private static void loadIfExists(Path file, ConfigBuilder builder) throws IOException {
        if (Files.isRegularFile(file)) {
            parse(Files.readAllLines(file, StandardCharsets.UTF_8), builder);
        }
    }

    static JspecifyConfig parse(List<String> lines) {
        ConfigBuilder builder = new ConfigBuilder();
        parse(lines, builder);
        return builder.build();
    }

    private static void parse(List<String> lines, ConfigBuilder builder) {
        List<String> path = new ArrayList<>();
        String listTarget = "";

        for (String rawLine : lines) {
            String withoutComment = stripComment(rawLine);
            if (withoutComment.isBlank()) {
                continue;
            }

            int indent = countIndent(withoutComment);
            String line = withoutComment.trim();
            int level = indent / 2;
            trimPath(path, level);

            if (line.startsWith("- ")) {
                String value = unquote(line.substring(2).trim());
                if (listTarget.equals("reports.formats")) {
                    builder.reportFormats.add(value);
                } else if (listTarget.equals("migration.generatedCode.patterns")) {
                    builder.generatedCodeExcludes.add(value);
                } else if (listTarget.equals("sourceRoots")) {
                    builder.sourceRoots.add(Path.of(value));
                } else if (listTarget.equals("packagePolicy.markPackages")) {
                    builder.markPackages.add(value);
                } else if (listTarget.equals("packagePolicy.leaveUnmarked")) {
                    builder.leaveUnmarkedPackages.add(value);
                } else if (listTarget.equals("publicApi.include")) {
                    builder.publicApiIncludes.add(value);
                } else if (listTarget.equals("publicApi.exclude")) {
                    builder.publicApiExcludes.add(value);
                } else if (listTarget.equals("nullaway.annotatedPackages")) {
                    builder.nullawayAnnotatedPackages.add(value);
                } else if (listTarget.equals("nullaway.excludedClasses")) {
                    builder.nullawayExcludedClasses.add(value);
                }
                continue;
            }

            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }

            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            trimPath(path, level);
            path.add(key);
            String dotted = String.join(".", path);
            listTarget = "";

            if (value.isEmpty()) {
                listTarget = dotted;
                continue;
            }

            if (dotted.equals("jspecify.version")) {
                builder.jspecifyVersion = unquote(value);
            } else if (dotted.equals("reports.formats")) {
                builder.reportFormats.clear();
                builder.reportFormats.addAll(parseInlineList(value));
            } else if (dotted.equals("reports.outputDirectory")) {
                builder.reportsOutputDirectory = Path.of(unquote(value));
            } else if (dotted.equals("scanner.followSymlinks")) {
                builder.followSymlinks = Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
            } else if (dotted.equals("publicApi.jpmsExportsOnly")) {
                builder.publicApiJpmsExportsOnly = Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
            } else if (dotted.equals("nullaway.enabled")) {
                builder.nullawayEnabled = Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
            } else if (dotted.equals("nullaway.mode")) {
                builder.nullawayMode = unquote(value);
            } else if (dotted.equals("kotlinVerification.enabled")) {
                builder.kotlinVerificationEnabled = Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
            } else if (dotted.equals("kotlinVerification.failOnWarnings")) {
                builder.kotlinVerificationFailOnWarnings = Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
            } else if (dotted.equals("kotlinVerification.generatedTestsDirectory")) {
                builder.kotlinVerificationGeneratedTestsDirectory = Path.of(unquote(value));
            } else if (dotted.startsWith("annotations.mappings.")) {
                String annotation = dotted.substring("annotations.mappings.".length());
                builder.annotationMappings.put(annotation, unquote(value));
            }
        }
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            }
            if (c == '#' && !quoted) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static int countIndent(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private static void trimPath(List<String> path, int level) {
        while (path.size() > level) {
            path.remove(path.size() - 1);
        }
    }

    private static List<String> parseInlineList(String value) {
        String trimmed = value.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return List.of(unquote(trimmed));
        }
        String body = trimmed.substring(1, trimmed.length() - 1);
        if (body.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String token : body.split(",")) {
            result.add(unquote(token.trim()));
        }
        return result;
    }

    private static String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2
                && ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static final class ConfigBuilder {
        private String jspecifyVersion = "1.0.0";
        private final Map<String, String> annotationMappings =
                new LinkedHashMap<>(AnnotationCatalog.defaults().mappings());
        private final List<String> reportFormats = new ArrayList<>(
                List.of("console", "html", "markdown", "sarif", "json"));
        private Path reportsOutputDirectory = Path.of("build/reports/jml/jspecify");
        private final List<String> generatedCodeExcludes = new ArrayList<>(
                List.of("**/generated/**", "**/target/generated-sources/**",
                        "**/build/generated/**"));
        private final List<Path> sourceRoots = new ArrayList<>();
        private final List<String> markPackages = new ArrayList<>();
        private final List<String> leaveUnmarkedPackages = new ArrayList<>();
        private final List<String> publicApiIncludes = new ArrayList<>();
        private final List<String> publicApiExcludes = new ArrayList<>();
        private boolean publicApiJpmsExportsOnly;
        private boolean nullawayEnabled;
        private String nullawayMode = "warn";
        private final List<String> nullawayAnnotatedPackages = new ArrayList<>();
        private final List<String> nullawayExcludedClasses = new ArrayList<>();
        private boolean kotlinVerificationEnabled;
        private boolean kotlinVerificationFailOnWarnings;
        private Path kotlinVerificationGeneratedTestsDirectory =
                Path.of("build/jspecify-kotlin-verification");
        private boolean followSymlinks;

        private JspecifyConfig build() {
            return new JspecifyConfig(jspecifyVersion, annotationMappings, reportFormats,
                    reportsOutputDirectory, generatedCodeExcludes, sourceRoots,
                    markPackages, leaveUnmarkedPackages, publicApiIncludes, publicApiExcludes,
                    publicApiJpmsExportsOnly, nullawayEnabled, nullawayMode,
                    nullawayAnnotatedPackages, nullawayExcludedClasses,
                    kotlinVerificationEnabled, kotlinVerificationFailOnWarnings,
                    kotlinVerificationGeneratedTestsDirectory, followSymlinks);
        }
    }
}
