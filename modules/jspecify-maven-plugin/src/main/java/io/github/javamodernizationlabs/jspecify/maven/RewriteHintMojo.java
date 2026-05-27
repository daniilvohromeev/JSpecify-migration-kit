package io.github.javamodernizationlabs.jspecify.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "rewrite-hint", threadSafe = true, requiresProject = false)
public class RewriteHintMojo extends AbstractMojo {

    @Parameter(property = "jspecify.recipe",
            defaultValue = "io.github.jml.jspecify.Migrate")
    private String recipe;

    @Parameter(property = "jspecify.apply", defaultValue = "false")
    private boolean apply;

    @Override
    public void execute() {
        String goal = apply ? "run" : "dryRun";
        getLog().info("");
        getLog().info("To " + (apply ? "apply" : "preview") + " recipe '" + recipe + "' run:");
        getLog().info("");
        getLog().info("  mvn org.openrewrite.maven:rewrite-maven-plugin:" + goal + " \\");
        getLog().info("    -Drewrite.activeRecipes=" + recipe);
        getLog().info("");
        getLog().info("Configure the OpenRewrite plugin to depend on:");
        getLog().info("  io.github.javamodernizationlabs:jspecify-migration-rewrite-recipes");
    }
}
