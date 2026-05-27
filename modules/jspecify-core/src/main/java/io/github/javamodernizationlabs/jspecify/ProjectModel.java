package io.github.javamodernizationlabs.jspecify;

import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public record ProjectModel(
        String name,
        Path rootDirectory,
        List<Path> sourceRoots,
        List<String> excludedPathPatterns,
        boolean followSymlinks
) {
    public ProjectModel {
        rootDirectory = rootDirectory.toAbsolutePath().normalize();
        sourceRoots = sourceRoots == null ? List.of() : List.copyOf(sourceRoots);
        excludedPathPatterns = excludedPathPatterns == null
                ? List.of()
                : List.copyOf(excludedPathPatterns);
    }

    public static ProjectModel of(Path rootDirectory) {
        return of(rootDirectory, JspecifyConfig.defaults());
    }

    public static ProjectModel of(Path rootDirectory, JspecifyConfig config) {
        Path root = rootDirectory.toAbsolutePath().normalize();
        return new ProjectModel(rootDirectory.getFileName() == null
                ? rootDirectory.toString()
                : rootDirectory.getFileName().toString(),
                root,
                discoverSourceRoots(root, config.sourceRoots()),
                config.generatedCodeExcludes(),
                config.followSymlinks());
    }

    public static ProjectModel of(Path rootDirectory,
                                  List<Path> sourceRoots,
                                  List<String> excludedPathPatterns,
                                  boolean followSymlinks) {
        Path root = rootDirectory.toAbsolutePath().normalize();
        List<Path> normalizedRoots = sourceRoots == null
                ? discoverSourceRoots(root, List.of())
                : sourceRoots.stream()
                .map(p -> p.isAbsolute() ? p : root.resolve(p))
                .map(Path::normalize)
                .toList();
        return new ProjectModel(root.getFileName() == null
                ? root.toString()
                : root.getFileName().toString(),
                root,
                normalizedRoots,
                excludedPathPatterns,
                followSymlinks);
    }

    private static List<Path> discoverSourceRoots(Path root, List<Path> configuredRoots) {
        if (configuredRoots != null && !configuredRoots.isEmpty()) {
            return configuredRoots.stream()
                    .map(p -> p.isAbsolute() ? p : root.resolve(p))
                    .map(Path::normalize)
                    .toList();
        }

        Set<Path> roots = new LinkedHashSet<>();
        addIfDirectory(roots, root.resolve("src/main/java"));
        addIfDirectory(roots, root.resolve("src/test/java"));

        if (Files.isDirectory(root)) {
            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(Files::isDirectory)
                        .filter(ProjectModel::isConventionalJavaSourceRoot)
                        .filter(p -> !isBuildOutput(root, p))
                        .map(Path::normalize)
                        .forEach(roots::add);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to discover Java source roots under " + root, e);
            }
        }

        return new ArrayList<>(roots);
    }

    private static void addIfDirectory(Set<Path> roots, Path candidate) {
        if (Files.isDirectory(candidate)) {
            roots.add(candidate.normalize());
        }
    }

    private static boolean isConventionalJavaSourceRoot(Path path) {
        int count = path.getNameCount();
        if (count < 3) {
            return false;
        }
        return path.getName(count - 3).toString().equals("src")
                && (path.getName(count - 2).toString().equals("main")
                || path.getName(count - 2).toString().equals("test"))
                && path.getName(count - 1).toString().equals("java");
    }

    private static boolean isBuildOutput(Path root, Path path) {
        Path rel = root.relativize(path.toAbsolutePath().normalize());
        for (Path part : rel) {
            String name = part.toString();
            if (name.equals("build") || name.equals("target") || name.equals(".gradle")
                    || name.equals(".git")) {
                return true;
            }
        }
        return false;
    }
}
