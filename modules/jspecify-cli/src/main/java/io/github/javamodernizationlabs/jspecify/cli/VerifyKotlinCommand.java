package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.ProjectModel;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import io.github.javamodernizationlabs.jspecify.kotlin.KotlinInteropVerifier;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "verify-kotlin",
        description = "Generate Kotlin interop verification artifacts.",
        mixinStandardHelpOptions = true
)
public class VerifyKotlinCommand implements Callable<Integer> {

    @Option(names = {"--project"}, defaultValue = ".") Path project;
    @Option(names = {"--generate-samples"}) boolean generateSamples;
    @Option(names = {"--compile"}) boolean compile;
    @Option(names = {"--classpath"}, split = ",") List<Path> classpath;
    @Option(names = {"--output-dir"}) Path output;

    @Override
    public Integer call() throws Exception {
        Path projectRoot = project.toAbsolutePath().normalize();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        Path out = output == null
                ? resolveGeneratedTestsDirectory(projectRoot, config)
                : output.toAbsolutePath().normalize();
        Files.createDirectories(out);
        var result = new KotlinInteropVerifier().verify(ProjectModel.of(projectRoot, config),
                out, generateSamples || config.kotlinVerificationEnabled(), compile,
                effectiveClasspath(projectRoot));
        System.out.println("Kotlin verification artifacts written to " + out);
        if (!result.warnings().isEmpty()) {
            result.warnings().forEach(warning -> System.err.println("warning: " + warning));
        }
        return 0;
    }

    private Path resolveGeneratedTestsDirectory(Path projectRoot, JspecifyConfig config) {
        Path configured = config.kotlinVerificationGeneratedTestsDirectory();
        return configured.isAbsolute()
                ? configured.normalize()
                : projectRoot.resolve(configured).normalize();
    }

    private List<Path> effectiveClasspath(Path projectRoot) {
        if (classpath != null && !classpath.isEmpty()) {
            return classpath.stream()
                    .map(path -> path.isAbsolute() ? path : projectRoot.resolve(path))
                    .map(Path::normalize)
                    .toList();
        }
        List<Path> detected = new ArrayList<>();
        List.of(projectRoot.resolve("build/classes/java/main"),
                projectRoot.resolve("target/classes"))
                .stream()
                .filter(Files::isDirectory)
                .forEach(detected::add);
        return detected;
    }
}
