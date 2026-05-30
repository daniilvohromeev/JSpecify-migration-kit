package io.github.javamodernizationlabs.jspecify;

import java.util.Objects;

/**
 * Stable identifier of an analysis rule that produced an {@link Issue}.
 *
 * @param value the non-blank rule identifier string
 */
public record RuleId(String value) {
    /**
     * Canonical constructor that validates the identifier.
     *
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public RuleId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("RuleId must not be blank");
        }
    }

    /**
     * Creates a rule identifier from the given string.
     *
     * @param value the non-blank rule identifier string
     * @return a new {@code RuleId} wrapping {@code value}
     */
    public static RuleId of(String value) {
        return new RuleId(value);
    }

    /**
     * Returns the raw identifier string.
     *
     * @return the rule identifier value
     */
    @Override
    public String toString() {
        return value;
    }
}
