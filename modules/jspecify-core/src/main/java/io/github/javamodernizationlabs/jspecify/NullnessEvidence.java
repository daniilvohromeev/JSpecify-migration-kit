package io.github.javamodernizationlabs.jspecify;

/**
 * Explains how a {@link Nullness} value was determined.
 *
 * @param source the origin of the evidence, e.g. {@code "annotation"} or {@code "inference"}
 * @param detail a description of the evidence, such as an annotation name or reasoning
 */
public record NullnessEvidence(String source, String detail) {
    /**
     * Creates evidence attributing nullness to an explicit annotation.
     *
     * @param fqn the fully qualified name of the annotation
     * @return evidence with source {@code "annotation"} and the given detail
     */
    public static NullnessEvidence fromAnnotation(String fqn) {
        return new NullnessEvidence("annotation", fqn);
    }

    /**
     * Creates evidence attributing nullness to inference.
     *
     * @param reason a description of the inference reasoning
     * @return evidence with source {@code "inference"} and the given detail
     */
    public static NullnessEvidence fromInference(String reason) {
        return new NullnessEvidence("inference", reason);
    }
}
