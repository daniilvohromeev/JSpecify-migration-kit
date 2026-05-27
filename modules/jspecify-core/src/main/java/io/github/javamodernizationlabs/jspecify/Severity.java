package io.github.javamodernizationlabs.jspecify;

public enum Severity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public boolean atLeast(Severity other) {
        return ordinal() >= other.ordinal();
    }

    public static Severity parse(String raw) {
        if (raw == null) {
            return INFO;
        }
        return Severity.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
