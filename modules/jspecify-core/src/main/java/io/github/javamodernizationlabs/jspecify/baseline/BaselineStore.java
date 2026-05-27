package io.github.javamodernizationlabs.jspecify.baseline;

import io.github.javamodernizationlabs.jspecify.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class BaselineStore {

    private static final Pattern FINGERPRINT =
            Pattern.compile("\"fingerprint\"\\s*:\\s*\"([^\"]+)\"");

    public Set<String> read(Path baselineFile) throws IOException {
        if (baselineFile == null || !Files.isRegularFile(baselineFile)) {
            return Set.of();
        }
        String content = Files.readString(baselineFile, StandardCharsets.UTF_8);
        Set<String> fingerprints = new LinkedHashSet<>();
        var matcher = FINGERPRINT.matcher(content);
        while (matcher.find()) {
            fingerprints.add(matcher.group(1));
        }
        return fingerprints;
    }

    public List<Issue> newIssues(List<Issue> issues, Path baselineFile) throws IOException {
        Set<String> baseline = read(baselineFile);
        return issues.stream()
                .filter(issue -> !baseline.contains(issue.fingerprint()))
                .toList();
    }

    public void write(Path baselineFile, List<Issue> issues) throws IOException {
        if (baselineFile.getParent() != null) {
            Files.createDirectories(baselineFile.getParent());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fingerprints\":[");
        for (int i = 0; i < issues.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"fingerprint\":\"").append(issues.get(i).fingerprint()).append("\"}");
        }
        sb.append("]}\n");
        Files.writeString(baselineFile, sb.toString(), StandardCharsets.UTF_8);
    }
}
