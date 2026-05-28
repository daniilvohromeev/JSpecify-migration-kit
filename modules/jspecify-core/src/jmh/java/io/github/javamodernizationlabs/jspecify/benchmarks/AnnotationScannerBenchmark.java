package io.github.javamodernizationlabs.jspecify.benchmarks;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.scan.AnnotationScanner;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class AnnotationScannerBenchmark {

    private AnnotationScanner scanner;
    private ProjectModel project;
    private Path projectRoot;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        projectRoot = Files.createTempDirectory("jspecify-scanner-benchmark");
        Path packageDir = projectRoot.resolve("src/main/java/com/acme/api");
        Files.createDirectories(packageDir);
        for (int i = 0; i < 12; i++) {
            Files.writeString(packageDir.resolve("Api" + i + ".java"), source(i),
                    StandardCharsets.UTF_8);
        }
        scanner = new AnnotationScanner();
        project = ProjectModel.of(projectRoot);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        if (projectRoot == null || !Files.exists(projectRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Benchmark
    public int scanSampleProject() throws IOException {
        return scanner.scan(project).totalAnnotations();
    }

    private static String source(int index) {
        return """
                package com.acme.api;

                import org.jetbrains.annotations.Nullable;
                import org.springframework.lang.NonNull;

                public class Api%s {
                    @Nullable
                    public String nickname(@Nullable String fallback) {
                        return fallback;
                    }

                    @NonNull
                    public String name() {
                        return "api";
                    }
                }
                """.formatted(index);
    }
}
