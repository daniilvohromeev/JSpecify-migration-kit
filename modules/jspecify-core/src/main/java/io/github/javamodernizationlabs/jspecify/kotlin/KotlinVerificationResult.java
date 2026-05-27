package io.github.javamodernizationlabs.jspecify.kotlin;

import java.nio.file.Path;
import java.util.List;

public record KotlinVerificationResult(
        Path outputDirectory,
        Path sampleFile,
        boolean samplesGenerated,
        boolean compileRequested,
        String compileStatus,
        List<String> warnings
) {
    public KotlinVerificationResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
