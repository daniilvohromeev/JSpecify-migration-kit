package io.github.javamodernizationlabs.jspecify;

import java.util.List;

public record Recommendation(String summary, List<String> links) {
    public Recommendation {
        links = links == null ? List.of() : List.copyOf(links);
    }

    public static Recommendation of(String summary) {
        return new Recommendation(summary, List.of());
    }

    public static Recommendation of(String summary, List<String> links) {
        return new Recommendation(summary, links);
    }
}
