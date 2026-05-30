package io.github.javamodernizationlabs.jspecify;

/**
 * The nullness contract attributed to a program element.
 */
public enum Nullness {
    /** The element is known to never be {@code null}. */
    NON_NULL,
    /** The element may be {@code null}. */
    NULLABLE,
    /** The nullness is explicitly left unspecified by JSpecify. */
    UNSPECIFIED,
    /** The nullness could not be determined. */
    UNKNOWN
}
