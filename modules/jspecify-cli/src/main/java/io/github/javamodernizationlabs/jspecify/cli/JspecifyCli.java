package io.github.javamodernizationlabs.jspecify.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "jml",
        description = "Java Modernization Labs command line tools",
        mixinStandardHelpOptions = true,
        version = "0.1.0-SNAPSHOT",
        subcommands = {
                JspecifyCli.JspecifyCommand.class
        }
)
public class JspecifyCli implements Runnable {

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
