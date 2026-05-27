package io.github.javamodernizationlabs.jspecify.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "explain",
        description = "Explain a JSpecify Migration Kit rule id.",
        mixinStandardHelpOptions = true
)
public class ExplainCommand implements Callable<Integer> {

    @Parameters(arity = "1", description = "Rule id, e.g. jspecify.old-nullness-annotation.")
    String ruleId;

    private static final Map<String, String> EXPLAIN = Map.of(
            "jspecify.old-nullness-annotation",
            """
            Rule: jspecify.old-nullness-annotation
            Severity: MEDIUM

            Why it matters:
            Legacy nullness annotations (JetBrains, JSR-305, Spring, FindBugs, ...) are
            either declaration-only or come with subtle semantic differences from JSpecify.

            Recommended actions:
              1. Run `jml jspecify rewrite --recipe convert-known-annotations --dry-run`
              2. Review ambiguous type-use placements.
              3. Add JSpecify dependency and apply the recipe.
            """,
            "jspecify.public-api-unspecified",
            """
            Rule: jspecify.public-api-unspecified
            Severity: MEDIUM

            Why it matters:
            Public API elements without an explicit JSpecify nullness contract are treated
            as UNSPECIFIED, which weakens null-safety guarantees for downstream consumers
            (including Kotlin clients).
            """,
            "jspecify.ambiguous-type-use",
            """
            Rule: jspecify.ambiguous-type-use
            Severity: HIGH

            Why it matters:
            JSpecify is a type-use annotation system: @Nullable List<String> means a
            nullable list, not a list of nullable strings. Migrating a declaration-only
            legacy annotation requires deciding the intended placement manually.
            """,
            "jspecify.package-conflicting-defaults",
            """
            Rule: jspecify.package-conflicting-defaults
            Severity: HIGH

            Why it matters:
            Package-level defaults such as @NonNullApi or @ParametersAreNonnullByDefault
            may not map cleanly to JSpecify @NullMarked semantics. Resolve the package
            policy manually before adding @NullMarked.
            """,
            "jspecify.kotlin-platform-type-leak",
            """
            Rule: jspecify.kotlin-platform-type-leak
            Severity: MEDIUM

            Why it matters:
            Kotlin callers see platform types when Java public API lacks an effective
            nullness contract. Add package-level @NullMarked or explicit type-use
            annotations to public API.
            """,
            "jspecify.nullaway-error",
            """
            Rule: jspecify.nullaway-error
            Severity: HIGH

            Why it matters:
            NullAway found a nullness violation after JSpecify migration. Fix the code,
            adjust the annotation contract, or baseline known legacy issues before
            enabling strict CI gates.
            """,
            "jspecify.generated-code-annotation",
            """
            Rule: jspecify.generated-code-annotation
            Severity: LOW

            Why it matters:
            Generated sources should usually be excluded from manual annotation edits.
            Configure generated-code excludes and prefer fixing the generator template.
            """
    );

    @Override
    public Integer call() {
        String body = EXPLAIN.get(ruleId);
        if (body == null) {
            System.err.println("Unknown rule id: " + ruleId);
            return 2;
        }
        System.out.println(body);
        return 0;
    }
}
