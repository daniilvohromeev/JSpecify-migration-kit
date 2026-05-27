package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class JspecifyRewriteHintTask extends DefaultTask {

    @Input
    public abstract Property<Boolean> getApply();

    @Input
    public abstract Property<String> getRecipe();

    @TaskAction
    public void run() {
        boolean apply = getApply().getOrElse(false);
        String recipe = getRecipe().getOrElse("io.github.jml.jspecify.Migrate");
        String task = apply ? "rewriteRun" : "rewriteDryRun";
        getLogger().lifecycle("");
        getLogger().lifecycle("To {} recipe '{}' run:", apply ? "apply" : "preview", recipe);
        getLogger().lifecycle("");
        getLogger().lifecycle("  ./gradlew {} \\", task);
        getLogger().lifecycle("    -Drewrite.activeRecipes={}", recipe);
        getLogger().lifecycle("");
        getLogger().lifecycle("Requires the OpenRewrite Gradle plugin and a rewrite dependency on:");
        getLogger().lifecycle("  io.github.javamodernizationlabs:jspecify-migration-rewrite-recipes");
    }
}
