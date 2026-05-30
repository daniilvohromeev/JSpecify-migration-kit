package io.github.javamodernizationlabs.jspecify;

import java.util.List;

/**
 * A path identifying a nested type-use position within a type expression.
 *
 * <p>Each index selects a child of the current type, e.g. a type argument, so the
 * path {@code [0]} refers to the first type argument and an empty path refers to
 * the root type itself.
 *
 * @param indices the sequence of child indices from the root; defensively copied
 */
public record TypeUsePath(List<Integer> indices) {
    /**
     * Canonical constructor that defensively copies the indices and substitutes an
     * empty list for a {@code null} argument.
     */
    public TypeUsePath {
        indices = indices == null ? List.of() : List.copyOf(indices);
    }

    /**
     * Returns the root path, which refers to the outermost type with no nesting.
     *
     * @return an empty type-use path
     */
    public static TypeUsePath root() {
        return new TypeUsePath(List.of());
    }

    /**
     * Returns a new path that extends this one with an additional child index.
     *
     * @param index the child index to append
     * @return a new path with {@code index} appended
     */
    public TypeUsePath plus(int index) {
        var copy = new java.util.ArrayList<>(indices);
        copy.add(index);
        return new TypeUsePath(copy);
    }
}
