package io.github.javamodernizationlabs.jspecify.maven;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullAwayCheckMojoTest {

    @Test
    void failsWhenEnabledProfileCannotRun(@TempDir Path tmp) throws Exception {
        NullAwayCheckMojo mojo = mojo(tmp, new MavenProject(), List.of("com.acme"));

        MojoExecutionException failure = assertThrows(MojoExecutionException.class, mojo::execute);

        assertTrue(failure.getMessage().contains("Error Prone/NullAway"));
    }

    @Test
    void writesReadyReportWhenCompilerProfileIsConfigured(@TempDir Path tmp) throws Exception {
        MavenProject project = new MavenProject(new Model());
        Build build = new Build();
        project.getModel().setBuild(build);
        Plugin compiler = new Plugin();
        compiler.setGroupId("org.apache.maven.plugins");
        compiler.setArtifactId("maven-compiler-plugin");
        compiler.setConfiguration("""
                <configuration>
                  <compilerArgs>
                    <arg>-Xplugin:ErrorProne</arg>
                    <arg>-Xep:NullAway:WARN</arg>
                  </compilerArgs>
                </configuration>
                """);
        Dependency nullaway = new Dependency();
        nullaway.setGroupId("com.uber.nullaway");
        nullaway.setArtifactId("nullaway");
        nullaway.setVersion("0.12.0");
        compiler.addDependency(nullaway);
        build.addPlugin(compiler);
        NullAwayCheckMojo mojo = mojo(tmp, project, List.of("com.acme"));

        mojo.execute();

        assertTrue(Files.readString(tmp.resolve("nullaway-check.md"))
                .contains("Status: `ready`"));
    }

    private NullAwayCheckMojo mojo(Path output, MavenProject project, List<String> packages)
            throws Exception {
        NullAwayCheckMojo mojo = new NullAwayCheckMojo();
        set(mojo, "project", project);
        set(mojo, "outputDirectory", output.toFile());
        set(mojo, "enabled", true);
        set(mojo, "mode", "warn");
        set(mojo, "annotatedPackages", packages);
        set(mojo, "excludedClasses", List.of());
        return mojo;
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
