package io.github.javamodernizationlabs.jspecify;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public record Issue(
        String tool,
        RuleId ruleId,
        Severity severity,
        String title,
        String message,
        Location location,
        List<String> evidence,
        Recommendation recommendation,
        String fingerprint
) {
    public Issue {
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(location, "location");
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        Objects.requireNonNull(recommendation, "recommendation");
        Objects.requireNonNull(fingerprint, "fingerprint");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String tool = "jspecify-migration-kit";
        private RuleId ruleId;
        private Severity severity = Severity.INFO;
        private String title = "";
        private String message = "";
        private Location location = Location.none();
        private List<String> evidence = List.of();
        private Recommendation recommendation = Recommendation.of("");

        public Builder tool(String tool) { this.tool = tool; return this; }
        public Builder ruleId(RuleId ruleId) { this.ruleId = ruleId; return this; }
        public Builder ruleId(String ruleId) { this.ruleId = RuleId.of(ruleId); return this; }
        public Builder severity(Severity severity) { this.severity = severity; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder location(Location location) { this.location = location; return this; }
        public Builder evidence(List<String> evidence) { this.evidence = evidence; return this; }
        public Builder recommendation(Recommendation recommendation) {
            this.recommendation = recommendation;
            return this;
        }

        public Issue build() {
            String fp = fingerprint(tool, ruleId, location, message);
            return new Issue(tool, ruleId, severity, title, message,
                    location, evidence, recommendation, fp);
        }
    }

    private static String fingerprint(String tool, RuleId ruleId,
                                      Location location, String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String seed = tool + "|" + ruleId + "|" + location.path() + "|"
                    + location.startLine() + "|" + message;
            md.update(seed.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
