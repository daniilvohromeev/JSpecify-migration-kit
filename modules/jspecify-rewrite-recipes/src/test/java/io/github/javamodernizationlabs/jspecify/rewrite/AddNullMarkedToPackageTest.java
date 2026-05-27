package io.github.javamodernizationlabs.jspecify.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AddNullMarkedToPackageTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddNullMarkedToPackage());
    }

    @Test
    void createsPackageInfoForDiscoveredPackage() {
        rewriteRun(
                java(
                        """
                        package com.acme;

                        public class Api {}
                        """,
                        spec -> spec.path("src/main/java/com/acme/Api.java")
                ),
                java(
                        doesNotExist(),
                        """
                        @NullMarked
                        package com.acme;

                        import org.jspecify.annotations.NullMarked;
                        """,
                        spec -> spec.path("src/main/java/com/acme/package-info.java")
                )
        );
    }

    @Test
    void updatesExistingPackageInfo() {
        rewriteRun(
                java(
                        """
                        package com.acme;
                        """,
                        """
                        @NullMarked
                        package com.acme;

                        import org.jspecify.annotations.NullMarked;
                        """,
                        spec -> spec.path("src/main/java/com/acme/package-info.java")
                )
        );
    }

    @Test
    void preservesExistingPackageAnnotations() {
        rewriteRun(
                java(
                        """
                        @Deprecated
                        package com.acme;
                        """,
                        """
                        @Deprecated
                        @NullMarked
                        package com.acme;

                        import org.jspecify.annotations.NullMarked;
                        """,
                        spec -> spec.path("src/main/java/com/acme/package-info.java")
                )
        );
    }

    @Test
    void doesNotOverrideConflictingDefaults() {
        rewriteRun(
                java(
                        """
                        @javax.annotation.ParametersAreNonnullByDefault
                        package com.acme;
                        """,
                        spec -> spec.path("src/main/java/com/acme/package-info.java")
                )
        );
    }

    @Test
    void doesNotCreatePackageInfoForGeneratedSources() {
        rewriteRun(
                java(
                        """
                        package com.acme.generated;

                        public class GeneratedApi {}
                        """,
                        spec -> spec.path(
                                "build/generated/sources/annotationProcessor/java/main/com/acme/generated/GeneratedApi.java")
                )
        );
    }
}
