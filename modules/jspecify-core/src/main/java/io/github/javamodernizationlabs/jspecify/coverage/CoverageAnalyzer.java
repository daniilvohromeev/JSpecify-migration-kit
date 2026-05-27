package io.github.javamodernizationlabs.jspecify.coverage;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;
import io.github.javamodernizationlabs.jspecify.ProjectModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class CoverageAnalyzer {

    private static final Pattern PUBLIC_TYPE = Pattern.compile(
            "\\bpublic\\s+(class|interface|record|enum|@interface)\\s+\\w+");
    private static final Pattern PUBLIC_MEMBER = Pattern.compile(
            "\\b(public|protected)\\s+[^=;{}]+\\([^)]*\\)\\s*(throws\\s+[\\w.,\\s]+)?[;{]");
    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern AMBIGUOUS = Pattern.compile("@\\w*Nullable\\s+\\w+[<].*[>]");

    public CoverageSummary analyze(ProjectModel project) throws IOException {
        int publicApi = 0;
        int specified = 0;
        int ambiguous = 0;
        Set<String> packagesSeen = new HashSet<>();
        Set<String> nullMarkedPackages = new HashSet<>();
        List<Path> javaFiles = new ArrayList<>();

        for (Path root : project.sourceRoots()) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .forEach(javaFiles::add);
            }
        }

        for (Path file : javaFiles) {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            String packageName = packageName(lines);
            if (!packageName.isBlank()) {
                packagesSeen.add(packageName);
            }
            if (file.getFileName().toString().equals("package-info.java")
                    && hasNullMarked(lines) && !packageName.isBlank()) {
                nullMarkedPackages.add(packageName);
            }
        }

        for (Path file : javaFiles) {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            String packageName = packageName(lines);
            boolean fileNullMarked = hasNullMarked(lines) || nullMarkedPackages.contains(packageName);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (AMBIGUOUS.matcher(line).find()) {
                    ambiguous++;
                }
                if (PUBLIC_TYPE.matcher(line).find() || PUBLIC_MEMBER.matcher(line).find()) {
                    publicApi++;
                    if (fileNullMarked || hasLocalNullness(lines, i)) {
                        specified++;
                    }
                }
            }
        }
        return new CoverageSummary(publicApi, specified, nullMarkedPackages.size(),
                packagesSeen.size(), ambiguous);
    }

    private String packageName(List<String> lines) {
        for (String line : lines) {
            var matcher = PACKAGE.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    private boolean hasNullMarked(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("@NullMarked")
                || line.contains("@" + AnnotationCatalog.JSPECIFY_NULL_MARKED));
    }

    private boolean hasLocalNullness(List<String> lines, int index) {
        int start = Math.max(0, index - 2);
        for (int i = start; i <= index; i++) {
            String line = lines.get(i);
            if (line.contains("@Nullable") || line.contains("@NonNull")
                    || line.contains("@" + AnnotationCatalog.JSPECIFY_NULLABLE)
                    || line.contains("@" + AnnotationCatalog.JSPECIFY_NON_NULL)) {
                return true;
            }
        }
        return false;
    }
}
