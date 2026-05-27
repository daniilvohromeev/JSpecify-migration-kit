package io.github.javamodernizationlabs.jspecify;

public record NullnessUsage(
        ElementKind elementKind,
        String owner,
        String signature,
        TypeUsePath typeUsePath,
        Nullness nullness,
        NullnessEvidence evidence
) {}
