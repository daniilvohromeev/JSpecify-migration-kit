package io.github.javamodernizationlabs.jspecify;

/**
 * The kind of program element a nullness annotation or usage applies to.
 */
public enum ElementKind {
    /** A package, e.g. a {@code package-info.java} declaration. */
    PACKAGE,
    /** A class, interface, enum, or record type. */
    CLASS,
    /** A method declaration. */
    METHOD,
    /** A method or constructor parameter. */
    PARAMETER,
    /** A field declaration. */
    FIELD,
    /** A method return type. */
    RETURN,
    /** A nested type use within a type expression, e.g. a type argument. */
    TYPE_USE
}
