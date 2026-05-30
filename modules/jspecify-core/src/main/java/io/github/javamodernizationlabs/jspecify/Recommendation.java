package io.github.javamodernizationlabs.jspecify;

import java.util.List;

/**
 * A suggested remediation for an {@link Issue}, with optional supporting links.
 *
 * @param summary a short description of the recommended action
 * @param links reference URLs supporting the recommendation; defensively copied
 */
public record Recommendation(String summary, List<String> links) {
    /**
     * Canonical constructor that defensively copies the links and substitutes an
     * empty list for a {@code null} argument.
     */
    public Recommendation {
        links = links == null ? List.of() : List.copyOf(links);
    }

    /**
     * Creates a recommendation with no supporting links.
     *
     * @param summary a short description of the recommended action
     * @return a recommendation with an empty link list
     */
    public static Recommendation of(String summary) {
        return new Recommendation(summary, List.of());
    }

    /**
     * Creates a recommendation with supporting links.
     *
     * @param summary a short description of the recommended action
     * @param links reference URLs supporting the recommendation
     * @return a recommendation carrying the given links
     */
    public static Recommendation of(String summary, List<String> links) {
        return new Recommendation(summary, links);
    }
}
