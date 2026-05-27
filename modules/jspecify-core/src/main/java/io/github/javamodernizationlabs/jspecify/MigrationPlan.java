package io.github.javamodernizationlabs.jspecify;

import java.util.List;

public record MigrationPlan(
        AnnotationInventory inventory,
        List<Phase> phases,
        Risk estimatedRisk,
        List<Issue> issues
) {
    public MigrationPlan {
        phases = phases == null ? List.of() : List.copyOf(phases);
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public enum Risk { LOW, MEDIUM, HIGH }

    public record Phase(int order, String title, String description, List<String> commands) {
        public Phase {
            commands = commands == null ? List.of() : List.copyOf(commands);
        }
    }
}
