package io.github.javamodernizationlabs.jspecify.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;

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

    public static class VersionProvider implements IVersionProvider {
        @Override
        public String[] getVersion() {
            String version = JspecifyCli.class.getPackage().getImplementationVersion();
            if (version == null || version.isBlank()) {
                version = "(development build)";
            }
            return new String[] { version };
        }
    }

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

    public static void main(String[] args) {
        int code = new CommandLine(new JspecifyCli()).execute(args);
        System.exit(code);
    }

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
        @Override
        public void run() {
            new CommandLine(this).usage(System.out);
        }
    }
}
