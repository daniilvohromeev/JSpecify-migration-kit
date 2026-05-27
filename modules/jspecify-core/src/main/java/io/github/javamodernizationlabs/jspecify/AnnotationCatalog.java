package io.github.javamodernizationlabs.jspecify;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default mapping from legacy nullness annotations to JSpecify counterparts.
 * Mirrors {@code jspecify.yml#annotations.mappings} from the spec (sec. 25).
 */
public final class AnnotationCatalog {

    public static final String JSPECIFY_NULLABLE = "org.jspecify.annotations.Nullable";
    public static final String JSPECIFY_NON_NULL = "org.jspecify.annotations.NonNull";
    public static final String JSPECIFY_NULL_MARKED = "org.jspecify.annotations.NullMarked";
    public static final String JSPECIFY_NULL_UNMARKED = "org.jspecify.annotations.NullUnmarked";

    private static final Map<String, String> DEFAULT_MAPPINGS = defaultMappings();

    private final Map<String, String> mappings;

    public AnnotationCatalog(Map<String, String> mappings) {
        this.mappings = Map.copyOf(mappings);
    }

    public static AnnotationCatalog defaults() {
        return new AnnotationCatalog(DEFAULT_MAPPINGS);
    }

    public Map<String, String> mappings() {
        return mappings;
    }

    public Set<String> knownLegacyAnnotations() {
        return mappings.keySet();
    }

    public Nullness targetSemantics(String legacyFqn) {
        String target = mappings.get(legacyFqn);
        if (target == null) {
            return Nullness.UNKNOWN;
        }
        return switch (target) {
            case JSPECIFY_NULLABLE -> Nullness.NULLABLE;
            case JSPECIFY_NON_NULL -> Nullness.NON_NULL;
            default -> Nullness.UNSPECIFIED;
        };
    }

    private static Map<String, String> defaultMappings() {
        var m = new LinkedHashMap<String, String>();
        // Nullable
        m.put("org.jetbrains.annotations.Nullable", JSPECIFY_NULLABLE);
        m.put("javax.annotation.Nullable", JSPECIFY_NULLABLE);
        m.put("javax.annotation.CheckForNull", JSPECIFY_NULLABLE);
        m.put("jakarta.annotation.Nullable", JSPECIFY_NULLABLE);
        m.put("org.springframework.lang.Nullable", JSPECIFY_NULLABLE);
        m.put("edu.umd.cs.findbugs.annotations.Nullable", JSPECIFY_NULLABLE);
        m.put("edu.umd.cs.findbugs.annotations.PossiblyNull", JSPECIFY_NULLABLE);
        m.put("androidx.annotation.Nullable", JSPECIFY_NULLABLE);
        m.put("android.support.annotation.Nullable", JSPECIFY_NULLABLE);
        m.put("com.android.annotations.Nullable", JSPECIFY_NULLABLE);
        m.put("org.checkerframework.checker.nullness.qual.Nullable", JSPECIFY_NULLABLE);
        m.put("io.reactivex.rxjava3.annotations.Nullable", JSPECIFY_NULLABLE);
        m.put("reactor.util.annotation.Nullable", JSPECIFY_NULLABLE);
        // NonNull
        m.put("org.jetbrains.annotations.NotNull", JSPECIFY_NON_NULL);
        m.put("javax.annotation.Nonnull", JSPECIFY_NON_NULL);
        m.put("jakarta.annotation.Nonnull", JSPECIFY_NON_NULL);
        m.put("org.springframework.lang.NonNull", JSPECIFY_NON_NULL);
        m.put("edu.umd.cs.findbugs.annotations.NonNull", JSPECIFY_NON_NULL);
        m.put("androidx.annotation.NonNull", JSPECIFY_NON_NULL);
        m.put("android.support.annotation.NonNull", JSPECIFY_NON_NULL);
        m.put("com.android.annotations.NonNull", JSPECIFY_NON_NULL);
        m.put("org.checkerframework.checker.nullness.qual.NonNull", JSPECIFY_NON_NULL);
        m.put("io.reactivex.rxjava3.annotations.NonNull", JSPECIFY_NON_NULL);
        m.put("lombok.NonNull", JSPECIFY_NON_NULL);
        m.put("reactor.util.annotation.NonNull", JSPECIFY_NON_NULL);
        return Map.copyOf(m);
    }
}
