package io.github.javamodernizationlabs.jspecify.report;

import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.Issue;
import io.github.javamodernizationlabs.jspecify.Location;
import io.github.javamodernizationlabs.jspecify.MigrationPlan;
import io.github.javamodernizationlabs.jspecify.Recommendation;
import io.github.javamodernizationlabs.jspecify.Severity;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportWritersTest {

    @Test
    void jsonReportIsValidShape() {
        var counts = new LinkedHashMap<String, Integer>();
        counts.put("org.jetbrains.annotations.Nullable", 3);
        var locations = new LinkedHashMap<String, List<Location>>();
        locations.put("org.jetbrains.annotations.Nullable",
                List.of(new Location(Path.of("src/main/java/com/acme/Foo.java"),
                        42, 5, 42, 13)));
        var inv = new AnnotationInventory(counts, locations, 1);

        List<Issue> issues = new ArrayList<>();
        issues.add(Issue.builder()
                .ruleId("jspecify.old-nullness-annotation")
                .severity(Severity.MEDIUM)
                .title("Legacy nullness annotation")
                .message("Replace org.jetbrains.annotations.Nullable with JSpecify.")
                .location(new Location(Path.of("Foo.java"), 42, 5, 42, 13))
                .evidence(List.of("org.jetbrains.annotations.Nullable"))
                .recommendation(Recommendation.of("Run convert recipe."))
                .build());
        var plan = new MigrationPlan(inv,
                new io.github.javamodernizationlabs.jspecify.MigrationPlanner()
                        .plan(inv).phases(),
                MigrationPlan.Risk.MEDIUM, issues);

        String json = new JsonReportWriter().render(plan);
        assertTrue(json.startsWith("{"));
        assertTrue(json.contains("\"estimatedRisk\":\"MEDIUM\""));
        assertTrue(json.contains("jspecify.old-nullness-annotation"));

        String md = new MarkdownReportWriter().render(plan);
        assertTrue(md.contains("# JSpecify Migration Plan"));
        assertTrue(md.contains("org.jetbrains.annotations.Nullable"));

        String sarif = new SarifReportWriter().render(plan);
        assertTrue(sarif.contains("\"version\":\"2.1.0\""));
        assertTrue(sarif.contains("jspecify.old-nullness-annotation"));

        String html = new HtmlReportWriter().render(plan);
        assertTrue(html.contains("<!doctype html>"));
        assertTrue(html.contains("JSpecify Migration Report"));

        String junit = new JunitXmlReportWriter().render(plan);
        assertTrue(junit.contains("<testsuite name=\"jspecify-migration\""));
        assertTrue(junit.contains("<testcase"));
    }
}
