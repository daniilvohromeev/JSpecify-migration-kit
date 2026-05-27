package io.github.javamodernizationlabs.jspecify.rewrite;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeCatalogTest {

    @Test
    void publishesFrameworkPresets() throws Exception {
        try (var in = getClass().getResourceAsStream("/META-INF/rewrite/jspecify.yml")) {
            assertNotNull(in);
            String catalog = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(catalog.contains("io.github.jml.jspecify.SpringPreset"));
            assertTrue(catalog.contains("io.github.jml.jspecify.ReactorPreset"));
            assertTrue(catalog.contains("io.github.jml.jspecify.MicrometerPreset"));
            assertTrue(catalog.contains("io.github.jml.jspecify.FixTypeUseAnnotationPlacement"));
            assertTrue(catalog.contains("@io.micrometer.common.lang.NonNullApi"));
        }
    }
}
