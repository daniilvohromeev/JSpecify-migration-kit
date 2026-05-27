package io.github.javamodernizationlabs.jspecify.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "rewrite-apply", threadSafe = true, requiresProject = true)
public class RewriteApplyMojo extends RewriteHintMojo {
    public RewriteApplyMojo() {
        super(true);
    }
}
