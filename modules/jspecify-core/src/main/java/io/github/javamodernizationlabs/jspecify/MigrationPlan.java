package io.github.javamodernizationlabs.jspecify;

import java.util.List;

/**
 * A staged plan for migrating a project to JSpecify.
 *
 * @param inventory the annotation inventory the plan was derived from
 * @param phases the ordered migration phases; defensively copied
 * @param estimatedRisk the overall estimated migration risk
 * @param issues the issues surfaced while building the plan; defensively copied
 */
public record MigrationPlan(
        AnnotationInventory inventory,
        List<Phase> phases,
        Risk estimatedRisk,
        List<Issue> issues
) {
    /**
     * Canonical constructor that defensively copies the lists and substitutes empty
     * lists for {@code null} arguments.
     */
    public MigrationPlan {
        phases = phases == null ? List.of() : List.copyOf(phases);
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    /**
     * The estimated overall risk of carrying out a migration.
     */
    public enum Risk {
        /** Low risk. */
        LOW,
        /** Medium risk. */
        MEDIUM,
        /** High risk. */
        HIGH
    }

    /**
     * A single step in a {@link MigrationPlan}.
     *
     * @param order the one-based ordering of this phase within the plan
     * @param title a short title for the phase
     * @param description a longer description of what the phase accomplishes
     * @param commands suggested commands to carry out the phase; defensively copied
     */
    public record Phase(int order, String title, String description, List<String> commands) {
        /**
         * Canonical constructor that defensively copies the command list and
         * substitutes an empty list for a {@code null} argument.
         */
        public Phase {
            commands = commands == null ? List.of() : List.copyOf(commands);
        }
    }
}
