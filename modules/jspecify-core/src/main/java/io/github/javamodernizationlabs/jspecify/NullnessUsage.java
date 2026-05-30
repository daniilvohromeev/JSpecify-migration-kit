package io.github.javamodernizationlabs.jspecify;

/**
 * The nullness contract observed at a specific program element.
 *
 * @param elementKind the kind of element the usage applies to
 * @param owner the fully qualified name of the declaring type or package
 * @param signature the signature of the member the usage belongs to
 * @param typeUsePath the path to the type-use position within the element's type
 * @param nullness the nullness contract at this position
 * @param evidence the evidence explaining how the nullness was determined
 */
public record NullnessUsage(
        ElementKind elementKind,
        String owner,
        String signature,
        TypeUsePath typeUsePath,
        Nullness nullness,
        NullnessEvidence evidence
) {}
