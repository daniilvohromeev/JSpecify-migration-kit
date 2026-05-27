package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JspecifyMigrationPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create(
                "jspecifyMigration", JspecifyMigrationExtension.class);
        extension.getJspecifyVersion().convention("1.0.0");
        extension.getAddNullMarked().convention(false);
        extension.getConvertKnownAnnotations().convention(true);
        extension.getReportsDirectory().convention(
                project.getLayout().getBuildDirectory().dir("reports/jml/jspecify"));
        extension.getMigration().getMode().convention("incremental");
        extension.getMigration().getDefaultScope().convention("public-api");
        extension.getMigration().getAddNullMarked().convention(false);
        extension.getMigration().getConvertKnownAnnotations().convention(true);
        extension.getMigration().getInferFromJavadocs().convention(false);
        extension.getNullaway().getEnabled().convention(true);
        extension.getNullaway().getMode().convention("warn");
        extension.getNullaway().getAnnotatedPackages().convention(java.util.List.of());
        extension.getNullaway().getExcludedClasses().convention(java.util.List.of());
        extension.getKotlinVerification().getEnabled().convention(false);
        extension.getKotlinVerification().getGeneratedSourceSet()
                .convention("jspecifyKotlinVerification");
        extension.getKotlinVerification().getFailOnWarnings().convention(false);
        extension.getReports().getHtmlRequired().convention(true);
        extension.getReports().getSarifRequired().convention(true);
        extension.getReports().getMarkdownRequired().convention(true);
        extension.getReports().getJsonRequired().convention(true);
        extension.getReports().getJunitXmlRequired().convention(true);

        project.getTasks().register("jspecifyPlan", JspecifyPlanTask.class, t -> {
            t.setGroup("verification");
            t.setDescription("Inventory legacy nullness annotations and emit a JSpecify "
                    + "migration plan.");
            t.getOutputDirectory().set(extension.getReportsDirectory());
        });

        project.getTasks().register("jspecifyReport", JspecifyPlanTask.class, t -> {
            t.setGroup("reporting");
            t.setDescription("Generate JSpecify JSON, Markdown and SARIF reports.");
            t.getOutputDirectory().set(extension.getReportsDirectory());
        });

        project.getTasks().register("jspecifyRewriteDryRun", JspecifyRewriteHintTask.class, t -> {
            t.setGroup("verification");
            t.setDescription("Show the command to preview JSpecify OpenRewrite recipes.");
            t.getApply().set(false);
            t.getRecipe().set("io.github.jml.jspecify.Migrate");
        });

        project.getTasks().register("jspecifyRewriteApply", JspecifyRewriteHintTask.class, t -> {
            t.setGroup("verification");
            t.setDescription("Show the command to apply JSpecify OpenRewrite recipes.");
            t.getApply().set(true);
            t.getRecipe().set("io.github.jml.jspecify.Migrate");
        });

        project.getTasks().register("jspecifyCoverage", JspecifyCoverageTask.class, t -> {
            t.setGroup("verification");
            t.setDescription("Generate a public API nullness coverage report.");
            t.getOutputDirectory().set(extension.getReportsDirectory());
        });

        project.getTasks().register("jspecifyNullAwayCheck", JspecifyNullAwayCheckTask.class, t -> {
            t.setGroup("verification");
            t.setDescription("Generate NullAway/Error Prone configuration for JSpecify migration.");
            t.getNullAwayEnabled().set(extension.getNullaway().getEnabled());
            t.getMode().set(extension.getNullaway().getMode());
            t.getAnnotatedPackages().set(extension.getNullaway().getAnnotatedPackages());
            t.getExcludedClasses().set(extension.getNullaway().getExcludedClasses());
            t.getOutputDirectory().set(extension.getReportsDirectory());
        });

        project.getTasks().register("jspecifyVerifyKotlin", JspecifyVerifyKotlinTask.class, t -> {
            t.setGroup("verification");
            t.setDescription("Generate Kotlin interop verification artifacts.");
            t.getKotlinVerificationEnabled().set(extension.getKotlinVerification().getEnabled());
            t.getGeneratedSourceSet().set(extension.getKotlinVerification().getGeneratedSourceSet());
            t.getFailOnWarnings().set(extension.getKotlinVerification().getFailOnWarnings());
            t.getOutputDirectory().set(extension.getReportsDirectory()
                    .map(dir -> dir.dir("kotlin-verification")));
        });
    }
}
