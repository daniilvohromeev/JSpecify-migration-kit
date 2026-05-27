package io.github.javamodernizationlabs.jspecify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationCatalogTest {

    @Test
    void mapsKnownNullableVariantsToJspecifyNullable() {
        AnnotationCatalog catalog = AnnotationCatalog.defaults();
        for (String legacy : new String[] {
                "org.jetbrains.annotations.Nullable",
                "javax.annotation.Nullable",
                "jakarta.annotation.Nullable",
                "org.springframework.lang.Nullable",
                "edu.umd.cs.findbugs.annotations.Nullable",
                "androidx.annotation.Nullable",
                "org.checkerframework.checker.nullness.qual.Nullable",
                "reactor.util.annotation.Nullable"
        }) {
            assertEquals(AnnotationCatalog.JSPECIFY_NULLABLE,
                    catalog.mappings().get(legacy),
                    "Unexpected mapping for " + legacy);
            assertEquals(Nullness.NULLABLE, catalog.targetSemantics(legacy));
        }
    }

    @Test
    void mapsKnownNonNullVariantsToJspecifyNonNull() {
        AnnotationCatalog catalog = AnnotationCatalog.defaults();
        for (String legacy : new String[] {
                "org.jetbrains.annotations.NotNull",
                "javax.annotation.Nonnull",
                "jakarta.annotation.Nonnull",
                "org.springframework.lang.NonNull",
                "edu.umd.cs.findbugs.annotations.NonNull",
                "androidx.annotation.NonNull",
                "org.checkerframework.checker.nullness.qual.NonNull",
                "lombok.NonNull"
        }) {
            assertEquals(AnnotationCatalog.JSPECIFY_NON_NULL,
                    catalog.mappings().get(legacy),
                    "Unexpected mapping for " + legacy);
            assertEquals(Nullness.NON_NULL, catalog.targetSemantics(legacy));
        }
    }

    @Test
    void unknownAnnotationsAreUnknown() {
        AnnotationCatalog catalog = AnnotationCatalog.defaults();
        assertEquals(Nullness.UNKNOWN, catalog.targetSemantics("com.acme.MyNullable"));
        assertTrue(catalog.knownLegacyAnnotations().size() > 15);
    }
}
