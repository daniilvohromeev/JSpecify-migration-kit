package io.github.javamodernizationlabs.jspecify.rewrite;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JspecifyRewriterTest {

    @Test
    void convertsKnownAnnotationsOnApply(@TempDir Path tmp) throws IOException {
        Path source = tmp.resolve("src/main/java/com/acme/Api.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source,
                """
                package com.acme;
                import org.jetbrains.annotations.Nullable;
                class Api { @Nullable String name() { return null; } }
                """);

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp), List.of("convert-known-annotations"), true);

        assertEquals(1, result.changedFiles());
        String updated = Files.readString(source);
        assertTrue(updated.contains("import org.jspecify.annotations.Nullable;"));
        assertTrue(updated.contains("@Nullable String name()"));
    }

    @Test
    void reportsUnsafeDefaultAnnotationsWithoutChangingThem(@TempDir Path tmp) throws IOException {
        Path source = tmp.resolve("src/main/java/com/acme/package-info.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source,
                """
                @NonNullApi
                package com.acme;
                import org.springframework.lang.NonNullApi;
                """);

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp), List.of("convert-known-annotations"), false);

        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("@NonNullApi")));
        assertTrue(Files.readString(source).contains("@NonNullApi"));
    }

    @Test
    void addsGradleDependency(@TempDir Path tmp) throws IOException {
        Path build = tmp.resolve("build.gradle.kts");
        Files.writeString(build, "plugins { java }\n");

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp), List.of("add-dependency"), true);

        assertEquals(1, result.changedFiles());
        assertTrue(Files.readString(build).contains("compileOnly(\"org.jspecify:jspecify:1.0.0\")"));
    }

    @Test
    void addsNullMarkedPackageInfo(@TempDir Path tmp) throws IOException {
        Path source = tmp.resolve("src/main/java/com/acme/Api.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "package com.acme; public class Api {}\n");

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp), List.of("add-null-marked"), true);

        Path packageInfo = tmp.resolve("src/main/java/com/acme/package-info.java");
        assertEquals(1, result.changedFiles());
        assertTrue(Files.readString(packageInfo).contains("@NullMarked"));
    }

    @Test
    void removesOldGradleAnnotationDependenciesWhenUsagesAreGone(@TempDir Path tmp)
            throws IOException {
        Path build = tmp.resolve("build.gradle.kts");
        Files.writeString(build,
                """
                dependencies {
                    compileOnly("org.jetbrains:annotations:26.0.1")
                    compileOnly("org.jspecify:jspecify:1.0.0")
                }
                """);
        Path source = tmp.resolve("src/main/java/com/acme/Api.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source,
                """
                package com.acme;
                import org.jspecify.annotations.Nullable;
                class Api { @Nullable String name() { return null; } }
                """);

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp),
                        List.of("remove-old-annotation-dependencies"), true);

        assertEquals(1, result.changedFiles());
        assertTrue(!Files.readString(build).contains("org.jetbrains:annotations"));
    }

    @Test
    void removesOldMavenAnnotationDependenciesWhenUsagesAreGone(@TempDir Path tmp)
            throws IOException {
        Path pom = tmp.resolve("pom.xml");
        Files.writeString(pom,
                """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.acme</groupId>
                  <artifactId>demo</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.code.findbugs</groupId>
                      <artifactId>jsr305</artifactId>
                      <version>3.0.2</version>
                    </dependency>
                    <dependency>
                      <groupId>org.jspecify</groupId>
                      <artifactId>jspecify</artifactId>
                      <version>1.0.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);
        Path source = tmp.resolve("src/main/java/com/acme/Api.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source,
                """
                package com.acme;
                import org.jspecify.annotations.Nullable;
                class Api { @Nullable String name() { return null; } }
                """);

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp),
                        List.of("remove-old-annotation-dependencies"), true);

        assertEquals(1, result.changedFiles());
        String updated = Files.readString(pom);
        assertTrue(!updated.contains("<artifactId>jsr305</artifactId>"));
        assertTrue(updated.contains("<artifactId>jspecify</artifactId>"));
    }

    @Test
    void reportsAmbiguousTypeUseWithoutGuessing(@TempDir Path tmp) throws IOException {
        Path source = tmp.resolve("src/main/java/com/acme/Api.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source,
                """
                package com.acme;
                import java.util.List;
                import org.jetbrains.annotations.Nullable;
                class Api { @Nullable List<String> names() { return List.of(); } }
                """);

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp),
                        List.of("fix-type-use-annotation-placement"), false);

        assertTrue(result.warnings().stream()
                .anyMatch(warning -> warning.contains("Ambiguous annotation migration")
                        && warning.contains("Manual review required")));
    }

    @Test
    void migrateIncludesAmbiguousTypeUseReview(@TempDir Path tmp) throws IOException {
        Path build = tmp.resolve("build.gradle.kts");
        Files.writeString(build, "plugins { java }\n");
        Path source = tmp.resolve("src/main/java/com/acme/Api.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source,
                """
                package com.acme;
                import java.util.List;
                import org.jetbrains.annotations.Nullable;
                class Api { @Nullable List<String> names() { return List.of(); } }
                """);

        RewriteResult result = new JspecifyRewriter()
                .rewrite(ProjectModel.of(tmp), List.of("migrate"), true);

        assertEquals(2, result.changedFiles());
        assertTrue(Files.readString(source).contains("import org.jspecify.annotations.Nullable;"));
        assertTrue(result.warnings().stream()
                .anyMatch(warning -> warning.contains("Ambiguous annotation migration")));
    }
}
