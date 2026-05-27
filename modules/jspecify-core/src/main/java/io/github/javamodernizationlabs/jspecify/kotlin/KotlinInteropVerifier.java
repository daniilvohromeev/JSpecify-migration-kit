package io.github.javamodernizationlabs.jspecify.kotlin;

import io.github.javamodernizationlabs.jspecify.ProjectModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class KotlinInteropVerifier {

    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern PUBLIC_TYPE = Pattern.compile(
            "\\bpublic\\s+(?:class|interface|record|enum)\\s+(\\w+)");
    private static final Pattern PUBLIC_NO_ARG_METHOD = Pattern.compile(
            "\\bpublic\\s+[^=;{}()]+\\s+(\\w+)\\s*\\(\\s*\\)\\s*(?:throws\\s+[\\w.,\\s]+)?[;{]");

    public KotlinVerificationResult verify(ProjectModel project,
                                           Path outputDirectory,
                                           boolean generateSamples,
                                           boolean compileSamples,
                                           List<Path> classpath) throws IOException {
        Files.createDirectories(outputDirectory);
        List<String> warnings = new ArrayList<>();
        Path sampleFile = outputDirectory.resolve("KotlinInteropSamples.kt");
        if (generateSamples) {
            Files.writeString(sampleFile, sampleSource(project), StandardCharsets.UTF_8);
        }
        String compileStatus = "not requested";
        if (compileSamples) {
            if (!generateSamples) {
                Files.writeString(sampleFile, sampleSource(project), StandardCharsets.UTF_8);
            }
            compileStatus = compile(sampleFile, outputDirectory, classpath, warnings);
        }
        writeReport(outputDirectory, sampleFile, generateSamples || compileSamples,
                compileSamples, compileStatus, warnings);
        return new KotlinVerificationResult(outputDirectory, sampleFile,
                generateSamples || compileSamples, compileSamples, compileStatus, warnings);
    }

    private String sampleSource(ProjectModel project) throws IOException {
        List<ApiType> apiTypes = publicApi(project);
        StringBuilder out = new StringBuilder();
        out.append("package jspecify.verification\n\n");
        out.append("@Suppress(\"UNUSED_PARAMETER\", \"UNUSED_VARIABLE\")\n");
        out.append("object KotlinInteropSamples {\n");
        for (ApiType type : apiTypes) {
            out.append("    fun verify").append(type.simpleName()).append("(api: ")
                    .append(type.qualifiedName()).append(") {\n");
            if (type.noArgMethods().isEmpty()) {
                out.append("        api.toString()\n");
            } else {
                for (String method : type.noArgMethods()) {
                    out.append("        val ").append(method).append("Value = api.")
                            .append(method).append("()\n");
                }
            }
            out.append("    }\n\n");
        }
        if (apiTypes.isEmpty()) {
            out.append("    fun noPublicApiDetected() = Unit\n");
        }
        out.append("}\n");
        return out.toString();
    }

    private List<ApiType> publicApi(ProjectModel project) throws IOException {
        List<ApiType> types = new ArrayList<>();
        for (Path root : project.sourceRoots()) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root)) {
                Iterable<Path> javaFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        ::iterator;
                for (Path file : javaFiles) {
                    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                    String packageName = packageName(lines);
                    Optional<String> typeName = publicTypeName(lines);
                    if (typeName.isEmpty()) {
                        continue;
                    }
                    String qualifiedName = packageName.isBlank()
                            ? typeName.get()
                            : packageName + "." + typeName.get();
                    types.add(new ApiType(qualifiedName, typeName.get(), publicNoArgMethods(lines)));
                }
            }
        }
        return types;
    }

    private String packageName(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = PACKAGE.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    private Optional<String> publicTypeName(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = PUBLIC_TYPE.matcher(line);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        }
        return Optional.empty();
    }

    private List<String> publicNoArgMethods(List<String> lines) {
        List<String> methods = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = PUBLIC_NO_ARG_METHOD.matcher(line);
            if (matcher.find()) {
                String method = matcher.group(1);
                if (!method.equals("if") && !method.equals("for") && !method.equals("while")) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    private String compile(Path sampleFile,
                           Path outputDirectory,
                           List<Path> classpath,
                           List<String> warnings) {
        if (!commandAvailable("kotlinc")) {
            warnings.add("kotlinc was not found on PATH; generated samples were not compiled.");
            return "skipped: kotlinc not found";
        }
        try {
            List<String> command = new ArrayList<>();
            command.add("kotlinc");
            command.add(sampleFile.toString());
            if (classpath != null && !classpath.isEmpty()) {
                command.add("-classpath");
                command.add(String.join(System.getProperty("path.separator"),
                        classpath.stream().map(Path::toString).toList()));
            }
            command.add("-d");
            command.add(outputDirectory.resolve("kotlin-verification.jar").toString());
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean exited = process.waitFor(Duration.ofSeconds(30).toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);
            String output = new String(process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            if (!exited) {
                process.destroyForcibly();
                warnings.add("kotlinc timed out after 30 seconds.");
                return "failed: timeout";
            }
            if (process.exitValue() != 0) {
                warnings.add(output.isBlank() ? "kotlinc failed." : output.strip());
                return "failed";
            }
            return "compiled";
        } catch (IOException e) {
            warnings.add("Unable to run kotlinc: " + e.getMessage());
            return "failed";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            warnings.add("kotlinc was interrupted.");
            return "failed";
        }
    }

    private boolean commandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "-version")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)
                    && process.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void writeReport(Path outputDirectory,
                             Path sampleFile,
                             boolean samplesGenerated,
                             boolean compileRequested,
                             String compileStatus,
                             List<String> warnings) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Kotlin interop verification\n\n");
        report.append("Samples generated: `").append(samplesGenerated).append("`\n");
        report.append("Sample file: `").append(sampleFile.getFileName()).append("`\n");
        report.append("Compile requested: `").append(compileRequested).append("`\n");
        report.append("Compile status: `").append(compileStatus).append("`\n");
        if (!warnings.isEmpty()) {
            report.append("\n## Warnings\n\n");
            for (String warning : warnings) {
                report.append("- ").append(warning.replace('\n', ' ')).append('\n');
            }
        }
        Files.writeString(outputDirectory.resolve("kotlin-verification.md"),
                report.toString(), StandardCharsets.UTF_8);
    }

    private record ApiType(String qualifiedName, String simpleName, List<String> noArgMethods) {}
}
