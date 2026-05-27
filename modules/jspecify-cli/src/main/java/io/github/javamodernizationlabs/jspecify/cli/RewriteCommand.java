package io.github.javamodernizationlabs.jspecify.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "rewrite",
        description = "Apply OpenRewrite recipes via the OpenRewrite Maven/Gradle plugin.",
        mixinStandardHelpOptions = true
)
public class RewriteCommand implements Callable<Integer> {

    @Option(names = {"--project"}, description = "Project root (default: current directory)",
            defaultValue = ".")
    Path project;

    @Option(names = {"--recipe"},
            description = "Recipe id, e.g. io.github.jml.jspecify.ConvertKnownAnnotations.",
            required = true)
    String recipe;

    @Option(names = {"--dry-run"}, description = "Only print the planned changes.")
    boolean dryRun;

    @Option(names = {"--apply"}, description = "Apply the changes in-place.")
    boolean apply;

    @Override
    public Integer call() {
        // v0.1 surface: recipes are shipped in the
        // io.github.javamodernizationlabs:jspecify-migration-rewrite-recipes
        // artifact. The CLI just instructs users how to run them via the
        // OpenRewrite Maven/Gradle plugin — embedding the OpenRewrite
        // executor is left for v0.2 (it requires resolving the target
        // project's classpath, which the Gradle/Maven plugins already do).
        String mode = dryRun ? "dryRun" : (apply ? "run" : "dryRun");
        System.out.printf("To apply recipe '%s' on %s, run:%n", recipe, project);
        System.out.println();
        System.out.println("  # Gradle");
        System.out.println("  ./gradlew rewriteRun \\");
        System.out.printf("    -Drewrite.activeRecipes=%s%n", recipe);
        System.out.println();
        System.out.println("  # Maven");
        System.out.println("  mvn org.openrewrite.maven:rewrite-maven-plugin:" + mode + " \\");
        System.out.printf("    -Drewrite.activeRecipes=%s%n", recipe);
        return 0;
    }
}
