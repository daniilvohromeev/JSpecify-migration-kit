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
    }
}
