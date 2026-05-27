package io.github.javamodernizationlabs.jspecify.cli;

import io.github.javamodernizationlabs.jspecify.config.JspecifyConfig;
import io.github.javamodernizationlabs.jspecify.config.JspecifyConfigLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    @Option(names = {"--output-dir"}) Path output;

    @Override
    public Integer call() throws Exception {
        Path projectRoot = project.toAbsolutePath().normalize();
        JspecifyConfig config = JspecifyConfigLoader.load(projectRoot);
        Path out = output == null
                ? config.resolveReportsOutputDirectory(projectRoot).resolve("kotlin-verification")
                : output.toAbsolutePath().normalize();
        Files.createDirectories(out);
        Files.writeString(out.resolve("kotlin-verification.md"),
                """
                # Kotlin interop verification

                Generate samples: `%s`
                Compile samples: `%s`

                The stable Kotlin compiler integration is tracked for the v0.2 verifier.
                """.formatted(generateSamples, compile),
                StandardCharsets.UTF_8);
        System.out.println("Kotlin verification artifacts written to " + out);
        return 0;
    }
}
