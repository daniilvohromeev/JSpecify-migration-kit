package io.github.javamodernizationlabs.jspecify.kotlin;

import java.nio.file.Path;
import java.util.List;

/**
 * Outcome of a Kotlin interop verification run.
 *
 * @param outputDirectory the directory the samples and report were written to
 * @param sampleFile the generated Kotlin sample file
 * @param samplesGenerated whether the sample file was written
 * @param compileRequested whether sample compilation was requested
 * @param compileStatus a short human-readable description of the compile
 *     outcome (for example {@code "compiled"}, {@code "not requested"} or a
 *     failure reason)
 * @param warnings any warnings collected during verification; never
 *     {@code null}
 */
public record KotlinVerificationResult(
        Path outputDirectory,
        Path sampleFile,
        boolean samplesGenerated,
        boolean compileRequested,
        String compileStatus,
        List<String> warnings
) {
    /**
     * Normalizes the warnings list to an immutable copy, substituting an empty
     * list for {@code null}.
     */
    public KotlinVerificationResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
