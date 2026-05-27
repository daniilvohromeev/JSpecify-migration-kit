package io.github.javamodernizationlabs.jspecify.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "rewrite-dry-run", threadSafe = true, requiresProject = true)
public class RewriteDryRunMojo extends RewriteHintMojo {
    public RewriteDryRunMojo() {
        super(false);
    }
}
