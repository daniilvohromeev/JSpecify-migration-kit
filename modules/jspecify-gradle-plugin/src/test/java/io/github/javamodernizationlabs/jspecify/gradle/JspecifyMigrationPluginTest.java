package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.tasks.compile.JavaCompile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JspecifyMigrationPluginTest {

    @Test
    void registersExpectedTasks() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(JspecifyMigrationPlugin.class);

        for (String taskName : new String[] {
                "jspecifyPlan", "jspecifyReport",
                "jspecifyRewriteDryRun", "jspecifyRewriteApply",
                "jspecifyCoverage", "jspecifyNullAwayCheck", "jspecifyVerifyKotlin"
        }) {
            assertNotNull(project.getTasks().findByName(taskName),
                    "Expected task missing: " + taskName);
        }
        assertNotNull(project.getExtensions().findByName("jspecifyMigration"));
    }

    @Test
    void nullAwayCheckFailsWhenProfileCannotRun() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(JspecifyMigrationPlugin.class);
        var task = (JspecifyNullAwayCheckTask) project.getTasks()
                .getByName("jspecifyNullAwayCheck");
        task.getAnnotatedPackages().set(List.of("com.acme"));

        var failure = assertThrows(org.gradle.api.GradleException.class, task::run);

        assertTrue(failure.getMessage().contains("Error Prone/NullAway"));
    }

    @Test
    void nullAwayCheckWritesReadyReportWhenProfileIsConfigured(@TempDir Path tmp)
            throws Exception {
        var project = ProjectBuilder.builder().withProjectDir(tmp.toFile()).build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(JspecifyMigrationPlugin.class);
        project.getConfigurations().create("errorprone");
        project.getDependencies().add("errorprone", "com.uber.nullaway:nullaway:0.12.0");
        project.getTasks().withType(JavaCompile.class).configureEach(task ->
                task.getOptions().getCompilerArgs().add("-Xplugin:ErrorProne"));
        var task = (JspecifyNullAwayCheckTask) project.getTasks()
                .getByName("jspecifyNullAwayCheck");
        task.getAnnotatedPackages().set(List.of("com.acme"));

        task.run();

        Path report = tmp.resolve("build/reports/jml/jspecify/nullaway-check.md");
        assertTrue(Files.readString(report).contains("Status: `ready`"));
    }
}
