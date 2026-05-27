package io.github.javamodernizationlabs.jspecify.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "jml-jspecify",
        description = "JSpecify Migration Kit",
        mixinStandardHelpOptions = true,
        version = "0.1.0-SNAPSHOT",
        subcommands = {
                PlanCommand.class,
                RewriteCommand.class,
                ReportCommand.class,
                ExplainCommand.class
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
}
