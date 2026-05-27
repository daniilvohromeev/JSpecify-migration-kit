package io.github.javamodernizationlabs.jspecify.coverage;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverageAnalyzerTest {

    @Test
    void estimatesPublicApiNullnessCoverage(@TempDir Path tmp) throws IOException {
        Path api = tmp.resolve("src/main/java/com/acme/api");
        Files.createDirectories(api);
        Files.writeString(api.resolve("package-info.java"),
                """
                @org.jspecify.annotations.NullMarked
                package com.acme.api;
                """);
        Files.writeString(api.resolve("UserApi.java"),
                """
                package com.acme.api;
                import org.jspecify.annotations.Nullable;
                public class UserApi {
                    public String name() { return ""; }
                    public @Nullable String nickname() { return null; }
                }
                """);

        CoverageSummary summary = new CoverageAnalyzer().analyze(ProjectModel.of(tmp));

        assertEquals(3, summary.publicApiElements());
        assertEquals(3, summary.specifiedPublicApiElements());
        assertEquals(1, summary.nullMarkedPackages());
        assertEquals(2, summary.publicMethods());
        assertEquals(2, summary.returnNullnessSpecified());
        assertTrue(summary.specifiedRatio() >= 1.0d);
    }

    @Test
    void publicApiIncludeMatchesBasePackage(@TempDir Path tmp) throws IOException {
        Path api = tmp.resolve("src/main/java/com/acme/api");
        Files.createDirectories(api);
        Files.writeString(api.resolve("UserApi.java"),
                """
                package com.acme.api;
                public class UserApi {}
                """);

        ProjectModel project = ProjectModel.of(tmp, List.of(tmp.resolve("src/main/java")),
                List.of(), List.of("com.acme.api.**"), List.of(), false, false);

        CoverageSummary summary = new CoverageAnalyzer().analyze(project);

        assertEquals(1, summary.publicApiElements());
    }
}
