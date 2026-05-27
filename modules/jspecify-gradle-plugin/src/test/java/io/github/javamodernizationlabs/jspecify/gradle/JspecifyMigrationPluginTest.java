package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JspecifyMigrationPluginTest {

    @Test
    void registersExpectedTasks() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(JspecifyMigrationPlugin.class);

        for (String taskName : new String[] {
                "jspecifyPlan", "jspecifyReport",
                "jspecifyRewriteDryRun", "jspecifyRewriteApply"
        }) {
            assertNotNull(project.getTasks().findByName(taskName),
                    "Expected task missing: " + taskName);
        }
        assertNotNull(project.getExtensions().findByName("jspecifyMigration"));
    }
}
