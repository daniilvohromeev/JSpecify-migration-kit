package io.github.javamodernizationlabs.jspecify;

import java.util.Objects;

public record RuleId(String value) {
    public RuleId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("RuleId must not be blank");
        }
    }

    public static RuleId of(String value) {
        return new RuleId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
