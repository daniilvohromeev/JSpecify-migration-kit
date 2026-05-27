package io.github.javamodernizationlabs.jspecify;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated counts of legacy nullness annotations encountered in a project.
 * Each entry pairs the source file path with the line where the annotation
 * was referenced. The map preserves insertion order so reports are stable.
 */
public record AnnotationInventory(
        Map<String, Integer> totalByAnnotation,
        Map<String, List<Location>> locationsByAnnotation,
        int filesScanned
) {
    public AnnotationInventory {
        totalByAnnotation = totalByAnnotation == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(totalByAnnotation);
        locationsByAnnotation = locationsByAnnotation == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(locationsByAnnotation);
    }

    public static AnnotationInventory empty() {
        return new AnnotationInventory(new LinkedHashMap<>(), new LinkedHashMap<>(), 0);
    }

    public int totalAnnotations() {
        return totalByAnnotation.values().stream().mapToInt(Integer::intValue).sum();
    }
}
