package io.github.javamodernizationlabs.jspecify.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ConvertKnownAnnotationsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConvertKnownAnnotations());
    }

    @Test
    void migratesJetbrainsNullableImports() {
        rewriteRun(
                java(
                        """
                        package com.acme;

                        import org.jetbrains.annotations.Nullable;

                        public class UserApi {
                            @Nullable
                            public String nickname() { return null; }
                        }
                        """,
                        """
                        package com.acme;

                        import org.jspecify.annotations.Nullable;

                        public class UserApi {
                            @Nullable
                            public String nickname() { return null; }
                        }
                        """
                )
        );
    }

    @Test
    void migratesJetbrainsNotNull() {
        rewriteRun(
                java(
                        """
                        package com.acme;

                        import org.jetbrains.annotations.NotNull;

                        public class UserApi {
                            @NotNull
                            public String name() { return ""; }
                        }
                        """,
                        """
                        package com.acme;

                        import org.jspecify.annotations.NonNull;

                        public class UserApi {
                            @NonNull
                            public String name() { return ""; }
                        }
                        """
                )
        );
    }
}
