package io.github.javamodernizationlabs.jspecify;

/**
 * The severity ranking of a reported {@link Issue}, ordered from least to most severe.
 */
public enum Severity {
    /** Informational only; no action required. */
    INFO,
    /** Low severity. */
    LOW,
    /** Medium severity. */
    MEDIUM,
    /** High severity. */
    HIGH,
    /** Critical severity; should be addressed urgently. */
    CRITICAL;

    /**
     * Tests whether this severity is at least as severe as another.
     *
     * @param other the severity to compare against
     * @return {@code true} if this severity ranks at or above {@code other}
     */
    public boolean atLeast(Severity other) {
        return ordinal() >= other.ordinal();
    }

    /**
     * Parses a severity from its textual name, case-insensitively.
     *
     * @param raw the severity name, or {@code null}
     * @return the matching severity, or {@link #INFO} if {@code raw} is {@code null}
     * @throws IllegalArgumentException if {@code raw} is non-null but not a valid severity name
     */
    public static Severity parse(String raw) {
        if (raw == null) {
            return INFO;
        }
        return Severity.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
