package io.github.javamodernizationlabs.jspecify;

import java.nio.file.Path;
import java.util.List;

public record ProjectModel(
        String name,
        Path rootDirectory,
        List<Path> sourceRoots
) {
    public ProjectModel {
        sourceRoots = sourceRoots == null ? List.of() : List.copyOf(sourceRoots);
    }

    public static ProjectModel of(Path rootDirectory) {
        return new ProjectModel(rootDirectory.getFileName() == null
                ? rootDirectory.toString()
                : rootDirectory.getFileName().toString(),
                rootDirectory,
                List.of(rootDirectory.resolve("src/main/java"),
                        rootDirectory.resolve("src/test/java")));
    }
}
