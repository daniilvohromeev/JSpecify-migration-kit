package io.github.javamodernizationlabs.jspecify.kotlin;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KotlinInteropVerifierTest {

    @Test
    void generatesKotlinSamplesForPublicApi(@TempDir Path tmp) throws Exception {
        Path api = tmp.resolve("src/main/java/com/acme");
        Files.createDirectories(api);
        Files.writeString(api.resolve("UserApi.java"),
                """
                package com.acme;
                public class UserApi {
                    public String name() { return ""; }
                }
                """);

        Path out = tmp.resolve("build/jspecify-kotlin-verification");
        KotlinVerificationResult result = new KotlinInteropVerifier()
                .verify(ProjectModel.of(tmp), out, true, false, List.of());

        String sample = Files.readString(result.sampleFile());
        assertTrue(sample.contains("com.acme.UserApi"));
        assertTrue(sample.contains("api.name()"));
        assertTrue(Files.readString(out.resolve("kotlin-verification.md"))
                .contains("Samples generated: `true`"));
    }
}
