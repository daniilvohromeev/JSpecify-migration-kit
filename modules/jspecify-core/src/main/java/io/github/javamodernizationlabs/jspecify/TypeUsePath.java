package io.github.javamodernizationlabs.jspecify;

import java.util.List;

public record TypeUsePath(List<Integer> indices) {
    public TypeUsePath {
        indices = indices == null ? List.of() : List.copyOf(indices);
    }

    public static TypeUsePath root() {
        return new TypeUsePath(List.of());
    }

    public TypeUsePath plus(int index) {
        var copy = new java.util.ArrayList<>(indices);
        copy.add(index);
        return new TypeUsePath(copy);
    }
}
