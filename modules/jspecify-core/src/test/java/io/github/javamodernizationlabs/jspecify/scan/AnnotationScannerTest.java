package io.github.javamodernizationlabs.jspecify.scan;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationScannerTest {

    @Test
    void countsJetbrainsAndSpringAnnotations(@TempDir Path tmp) throws IOException {
        Path mainJava = tmp.resolve("src/main/java/com/acme");
        Files.createDirectories(mainJava);
        Files.writeString(mainJava.resolve("UserApi.java"),
                """
                package com.acme;

                import org.jetbrains.annotations.Nullable;
                import org.springframework.lang.NonNull;

                public class UserApi {
                    @Nullable
                    public String nickname() { return null; }

                    @NonNull
                    public String name() { return ""; }

                    public String greet(@Nullable String prefix) {
                        return prefix == null ? "" : prefix;
                    }
                }
                """);

        AnnotationInventory inv = new AnnotationScanner().scan(ProjectModel.of(tmp));

        assertEquals(2,
                inv.totalByAnnotation().get("org.jetbrains.annotations.Nullable"));
        assertEquals(1,
                inv.totalByAnnotation().get("org.springframework.lang.NonNull"));
        assertTrue(inv.filesScanned() >= 1);
    }

    @Test
    void ignoresFilesWithoutKnownImports(@TempDir Path tmp) throws IOException {
        Path mainJava = tmp.resolve("src/main/java/com/acme");
        Files.createDirectories(mainJava);
        Files.writeString(mainJava.resolve("Plain.java"),
                "package com.acme; public class Plain { }");

        AnnotationInventory inv = new AnnotationScanner().scan(ProjectModel.of(tmp));
        assertEquals(0, inv.totalAnnotations());
    }

    @Test
    void countsFullyQualifiedAndWildcardImportedAnnotations(@TempDir Path tmp) throws IOException {
        Path mainJava = tmp.resolve("src/main/java/com/acme");
        Files.createDirectories(mainJava);
        Files.writeString(mainJava.resolve("Qualified.java"),
                """
                package com.acme;

                import org.jetbrains.annotations.*;

                public class Qualified {
                    // @Nullable in a comment should not count
                    @Nullable
                    public String one() { return null; }

                    public @javax.annotation.Nullable String two() { return null; }
                }
                """);

        AnnotationInventory inv = new AnnotationScanner().scan(ProjectModel.of(tmp));

        assertEquals(1,
                inv.totalByAnnotation().get("org.jetbrains.annotations.Nullable"));
        assertEquals(1,
                inv.totalByAnnotation().get("javax.annotation.Nullable"));
    }

    @Test
    void forConfigHonorsCustomAnnotationMappings(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("jspecify.yml"),
                """
                annotations:
                  mappings:
                    com.acme.Nullable: org.jspecify.annotations.Nullable
                """);
        Path mainJava = tmp.resolve("src/main/java/com/acme");
        Files.createDirectories(mainJava);
        Files.writeString(mainJava.resolve("Api.java"),
                """
                package com.acme;
                import com.acme.Nullable;
                public class Api { @Nullable String name() { return null; } }
                """);

        JspecifyConfig config = JspecifyConfigLoader.load(tmp);
        ProjectModel model = ProjectModel.of(tmp, config);

        // The default catalog has no knowledge of com.acme.Nullable, so a bare
        // scanner (the pre-fix behavior of report/Gradle/Maven entry points)
        // never counts it.
        assertNull(new AnnotationScanner().scan(model)
                .totalByAnnotation().get("com.acme.Nullable"));
        // A config-aware scanner honors the custom mapping.
        assertEquals(1, AnnotationScanner.forConfig(config).scan(model)
                .totalByAnnotation().get("com.acme.Nullable"));
    }

    @Test
    void discoversNestedModuleSourceRoots(@TempDir Path tmp) throws IOException {
        Path moduleJava = tmp.resolve("modules/foo/src/main/java/com/acme");
        Files.createDirectories(moduleJava);
        Files.writeString(moduleJava.resolve("Api.java"),
                """
                package com.acme;
                import org.jetbrains.annotations.Nullable;
                class Api { @Nullable String name() { return null; } }
                """);

        AnnotationInventory inv = new AnnotationScanner().scan(ProjectModel.of(tmp));

        assertEquals(1,
                inv.totalByAnnotation().get("org.jetbrains.annotations.Nullable"));
        assertEquals(1, inv.filesScanned());
    }
}
