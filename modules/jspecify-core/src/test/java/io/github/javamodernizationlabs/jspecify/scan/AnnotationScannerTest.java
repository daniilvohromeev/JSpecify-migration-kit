package io.github.javamodernizationlabs.jspecify.scan;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.ProjectModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
