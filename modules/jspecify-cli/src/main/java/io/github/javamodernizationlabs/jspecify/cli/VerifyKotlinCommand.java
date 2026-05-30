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

/**
 * The {@code verify-kotlin} command, which generates and optionally compiles Kotlin interop
 * verification artifacts for a migrated Java project.
 *
 * <p>It inspects the project rooted at {@code --project} and, when {@code --generate-samples}
 * is set (or sample generation is enabled in configuration), produces Kotlin sources that
 * exercise the project's public API nullness contracts under the directory selected by
 * {@code --output-dir}. When {@code --compile} is set the samples are compiled against the
 * classpath given by {@code --classpath} (auto-detected from common build output directories
 * when omitted). With {@code --fail-on-warnings} the command returns a non-zero exit code if
 * verification emits any warnings.</p>
 */
@Command(
        name = "verify-kotlin",
        description = "Generate Kotlin interop verification artifacts.",
        mixinStandardHelpOptions = true
)
public class VerifyKotlinCommand implements Callable<Integer> {

    /**
     * Creates a {@code VerifyKotlinCommand}.
     */
    public VerifyKotlinCommand() {
    }

    @Option(names = {"--project"}, defaultValue = ".") Path project;
    @Option(names = {"--generate-samples"}) boolean generateSamples;
    @Option(names = {"--compile"}) boolean compile;
    @Option(names = {"--fail-on-warnings"},
            description = "Return a non-zero exit code when Kotlin verification emits warnings.")
    boolean failOnWarnings;
    @Option(names = {"--classpath"}, split = ",") List<Path> classpath;
    @Option(names = {"--output-dir"}) Path output;

    /**
     * Generates the Kotlin interop verification artifacts, optionally compiles them and
     * reports any warnings.
     *
     * <p>Returns {@code 1} when warnings are produced and failing on warnings is requested,
     * and {@code 0} otherwise.</p>
     *
     * @return the process exit code: {@code 1} when warnings are present and failing on
     *         warnings is enabled, {@code 0} otherwise
     * @throws Exception if loading the project, generating samples or compiling them fails
     */
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
        boolean fail = failOnWarnings || config.kotlinVerificationFailOnWarnings();
        return fail && !result.warnings().isEmpty() ? 1 : 0;
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
