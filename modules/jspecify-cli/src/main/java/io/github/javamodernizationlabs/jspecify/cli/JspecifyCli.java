package io.github.javamodernizationlabs.jspecify.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;

/**
 * Top-level {@code jml} command line entry point for the Java Modernization Labs tools.
 *
 * <p>This command groups the available toolchains as subcommands; the only subcommand
 * registered today is {@code jspecify}, which exposes the JSpecify Migration Kit. When the
 * top-level command is invoked without a subcommand it prints the usage help. Standard help
 * and version options are mixed in, and the reported version is supplied by
 * {@link VersionProvider}.</p>
 */
@Command(
        name = "jml",
        description = "Java Modernization Labs command line tools",
        mixinStandardHelpOptions = true,
        versionProvider = JspecifyCli.VersionProvider.class,
        subcommands = {
                JspecifyCli.JspecifyCommand.class
        }
)
public class JspecifyCli implements Runnable {

    /**
     * Creates a {@code JspecifyCli} root command.
     */
    public JspecifyCli() {
    }

    /**
     * Supplies the version string reported by the {@code --version} option.
     *
     * <p>The version is taken from the implementation version recorded in the JAR manifest;
     * when running from a source build where no manifest version is present, a development
     * placeholder is reported instead.</p>
     */
    public static class VersionProvider implements IVersionProvider {

        /**
         * Creates a {@code VersionProvider}.
         */
        public VersionProvider() {
        }

        /**
         * Returns the version lines to display for the {@code --version} option.
         *
         * <p>Reads the implementation version from the package manifest and falls back to a
         * {@code "(development build)"} placeholder when no version is available.</p>
         *
         * @return a single-element array holding the resolved version string
         */
        @Override
        public String[] getVersion() {
            String version = JspecifyCli.class.getPackage().getImplementationVersion();
            if (version == null || version.isBlank()) {
                version = "(development build)";
            }
            return new String[] { version };
        }
    }

    /**
     * Runs the top-level command, printing usage help to standard output.
     *
     * <p>This is invoked when {@code jml} is called without a subcommand.</p>
     */
    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

    /**
     * Application entry point that parses arguments, dispatches to the matching subcommand,
     * and terminates the JVM with the resulting exit code.
     *
     * @param args the raw command line arguments passed by the launcher
     */
    public static void main(String[] args) {
        int code = new CommandLine(new JspecifyCli()).execute(args);
        System.exit(code);
    }

    /**
     * The {@code jspecify} subcommand that hosts the JSpecify Migration Kit commands.
     *
     * <p>It acts as a grouping container for the migration workflow subcommands
     * ({@code plan}, {@code rewrite}, {@code coverage}, {@code nullaway-config},
     * {@code verify-kotlin}, {@code report} and {@code explain}). Invoked on its own it
     * prints usage help.</p>
     */
    @Command(
            name = "jspecify",
            description = "JSpecify Migration Kit",
            mixinStandardHelpOptions = true,
            subcommands = {
                    PlanCommand.class,
                    RewriteCommand.class,
                    CoverageCommand.class,
                    NullAwayConfigCommand.class,
                    VerifyKotlinCommand.class,
                    ReportCommand.class,
                    ExplainCommand.class
            }
    )
    static class JspecifyCommand implements Runnable {

        /**
         * Runs the {@code jspecify} grouping command, printing usage help to standard output.
         *
         * <p>This is invoked when {@code jspecify} is called without a subcommand.</p>
         */
        @Override
        public void run() {
            new CommandLine(this).usage(System.out);
        }
    }
}
