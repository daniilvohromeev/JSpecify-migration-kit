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
}
