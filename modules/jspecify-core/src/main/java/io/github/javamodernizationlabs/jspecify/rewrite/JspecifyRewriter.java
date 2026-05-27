package io.github.javamodernizationlabs.jspecify.rewrite;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;
import io.github.javamodernizationlabs.jspecify.ProjectModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class JspecifyRewriter {

    private static final Set<String> UNSAFE_DEFAULT_ANNOTATIONS = Set.of(
            "ParametersAreNonnullByDefault",
            "NonNullApi",
            "NonNullFields",
            "TypeQualifierDefault",
            "DefaultAnnotation",
            "DefaultQualifier");

    private final AnnotationCatalog catalog;

    public JspecifyRewriter() {
        this(AnnotationCatalog.defaults());
    }

    public JspecifyRewriter(AnnotationCatalog catalog) {
        this.catalog = catalog;
    }

    public RewriteResult rewrite(ProjectModel project, List<String> rawRecipes, boolean apply)
            throws IOException {
        Set<String> recipes = normalizeRecipes(rawRecipes);
        List<RewriteChange> changes = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (recipes.contains("add-dependency")) {
            addDependency(project, apply, changes, warnings);
        }
        if (recipes.contains("convert-known-annotations")) {
            convertKnownAnnotations(project, apply, changes, warnings);
        }
        return new RewriteResult(apply, changes, warnings);
    }

    private Set<String> normalizeRecipes(List<String> rawRecipes) {
        Set<String> recipes = new LinkedHashSet<>();
        for (String raw : rawRecipes == null || rawRecipes.isEmpty()
                ? List.of("convert-known-annotations")
                : rawRecipes) {
            for (String token : raw.split(",")) {
                String normalized = token.trim().toLowerCase(Locale.ROOT);
                if (normalized.isBlank()) {
                    continue;
                }
                if (normalized.endsWith(".migrate") || normalized.equals("migrate")) {
                    recipes.add("add-dependency");
                    recipes.add("convert-known-annotations");
                } else if (normalized.endsWith(".addjspecifydependency")
                        || normalized.equals("add-dependency")
                        || normalized.equals("add-jspecify-dependency")) {
                    recipes.add("add-dependency");
                } else if (normalized.endsWith(".convertknownannotations")
                        || normalized.equals("convert-known-annotations")) {
                    recipes.add("convert-known-annotations");
                } else {
                    recipes.add(normalized);
                }
            }
        }
        return recipes;
    }

    private void addDependency(ProjectModel project,
                               boolean apply,
                               List<RewriteChange> changes,
                               List<String> warnings) throws IOException {
        Path gradleKts = project.rootDirectory().resolve("build.gradle.kts");
        Path gradleGroovy = project.rootDirectory().resolve("build.gradle");
        Path pom = project.rootDirectory().resolve("pom.xml");

        if (Files.isRegularFile(gradleKts)) {
            addGradleDependency(gradleKts, apply, changes);
        } else if (Files.isRegularFile(gradleGroovy)) {
            addGradleDependency(gradleGroovy, apply, changes);
        } else if (Files.isRegularFile(pom)) {
            addMavenDependency(pom, apply, changes);
        } else {
            warnings.add("No supported Gradle or Maven build file found for add-dependency.");
        }
    }

    private void addGradleDependency(Path file, boolean apply, List<RewriteChange> changes)
            throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        if (content.contains("org.jspecify:jspecify")) {
            return;
        }
        String dependency = file.getFileName().toString().endsWith(".kts")
                ? "    compileOnly(\"org.jspecify:jspecify:1.0.0\")\n"
                : "    compileOnly 'org.jspecify:jspecify:1.0.0'\n";
        String updated;
        if (content.contains("dependencies {")) {
            updated = content.replaceFirst("dependencies\\s*\\{",
                    "dependencies {\n" + dependency);
        } else {
            updated = content + "\n\ndependencies {\n" + dependency + "}\n";
        }
        if (apply) {
            Files.writeString(file, updated, StandardCharsets.UTF_8);
        }
        changes.add(new RewriteChange(file, "Add compileOnly JSpecify dependency", 1, List.of()));
    }

    private void addMavenDependency(Path file, boolean apply, List<RewriteChange> changes)
            throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        if (content.contains("<artifactId>jspecify</artifactId>")) {
            return;
        }
        String dependency = """
                  <dependency>
                    <groupId>org.jspecify</groupId>
                    <artifactId>jspecify</artifactId>
                    <version>1.0.0</version>
                    <scope>provided</scope>
                  </dependency>
                """;
        String updated = content.contains("<dependencies>")
                ? content.replaceFirst("<dependencies>", "<dependencies>\n" + dependency)
                : content.replaceFirst("</project>", "  <dependencies>\n" + dependency
                + "  </dependencies>\n</project>");
        if (apply) {
            Files.writeString(file, updated, StandardCharsets.UTF_8);
        }
        changes.add(new RewriteChange(file, "Add provided-scope JSpecify dependency", 1, List.of()));
    }

    private void convertKnownAnnotations(ProjectModel project,
                                         boolean apply,
                                         List<RewriteChange> changes,
                                         List<String> warnings) throws IOException {
        for (Path root : project.sourceRoots()) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root)) {
                Iterable<Path> files = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        ::iterator;
                for (Path file : files) {
                    convertFile(file, apply, changes, warnings);
                }
            }
        }
    }

    private void convertFile(Path file,
                             boolean apply,
                             List<RewriteChange> changes,
                             List<String> warnings) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        List<String> localWarnings = unsafeWarnings(file, content);
        String updated = content;
        int replacements = 0;

        for (var mapping : catalog.mappings().entrySet()) {
            String source = mapping.getKey();
            String target = mapping.getValue();
            Pattern sourcePattern = Pattern.compile("\\b" + Pattern.quote(source) + "\\b");
            int before = occurrences(sourcePattern, updated);
            if (before > 0) {
                updated = sourcePattern.matcher(updated).replaceAll(target);
                replacements += before;
            }
            String sourceSimple = source.substring(source.lastIndexOf('.') + 1);
            String targetSimple = target.substring(target.lastIndexOf('.') + 1);
            Pattern importPattern = Pattern.compile("import\\s+"
                    + Pattern.quote(target) + "\\s*;");
            if (importPattern.matcher(updated).find()) {
                updated = updated.replaceAll("@" + Pattern.quote(sourceSimple) + "\\b",
                        "@" + targetSimple);
            }
        }

        if (replacements > 0) {
            if (apply) {
                Files.writeString(file, updated, StandardCharsets.UTF_8);
            }
            changes.add(new RewriteChange(file,
                    "Convert known legacy nullness annotations to JSpecify",
                    replacements,
                    localWarnings));
        } else {
            warnings.addAll(localWarnings);
        }
    }

    private List<String> unsafeWarnings(Path file, String content) {
        List<String> warnings = new ArrayList<>();
        for (String annotation : UNSAFE_DEFAULT_ANNOTATIONS) {
            if (content.contains("@" + annotation)) {
                warnings.add(file + ": default/meta annotation @" + annotation
                        + " requires manual JSpecify package policy review.");
            }
        }
        return warnings;
    }

    private int occurrences(Pattern pattern, String text) {
        int count = 0;
        var matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
