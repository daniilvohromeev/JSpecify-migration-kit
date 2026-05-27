package io.github.javamodernizationlabs.jspecify;

public record NullnessEvidence(String source, String detail) {
    public static NullnessEvidence fromAnnotation(String fqn) {
        return new NullnessEvidence("annotation", fqn);
    }

    public static NullnessEvidence fromInference(String reason) {
        return new NullnessEvidence("inference", reason);
    }
}
