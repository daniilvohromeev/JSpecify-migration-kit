package io.github.javamodernizationlabs.jspecify.scan;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;
import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.Location;
import io.github.javamodernizationlabs.jspecify.ProjectModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Walks a project's Java sources and counts known nullness annotation usages.
 *
 * <p>The scanner is intentionally text-based (regex over imports and annotation
 * markers): it avoids a Java parsing dependency for the MVP and is forgiving
 * for files that don't compile yet. It under-reports compared to a full parser
 * but is consistent and fast enough for {@code jml jspecify plan}.
 */
public final class AnnotationScanner {

    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.]+|[\\w.]+\\.\\*)\\s*;");

    private final AnnotationCatalog catalog;

    public AnnotationScanner() {
        this(AnnotationCatalog.defaults());
    }

    public AnnotationScanner(AnnotationCatalog catalog) {
        this.catalog = catalog;
    }

    public AnnotationInventory scan(ProjectModel project) throws IOException {
        Map<String, Integer> totals = new LinkedHashMap<>();
        Map<String, List<Location>> locations = new LinkedHashMap<>();
        int files = 0;
        for (Path root : project.sourceRoots()) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root)) {
                Iterable<Path> javaFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> shouldScan(project, p))
                        .filter(p -> p.toString().endsWith(".java"))
                        ::iterator;
                for (Path file : javaFiles) {
                    files++;
                    scanFile(project.rootDirectory(), file, totals, locations);
                }
            }
        }
        return new AnnotationInventory(totals, locations, files);
    }

    void scanFile(Path projectRoot,
                  Path file,
                  Map<String, Integer> totals,
                  Map<String, List<Location>> locations) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Map<String, String> shortToFqn = new HashMap<>();
        Set<String> wildcardPackages = new LinkedHashSet<>();
        Set<String> knownAnnotations = knownAnnotations();
        // First pass: resolve imports of known annotations and their short names.
        for (String line : lines) {
            Matcher m = IMPORT_PATTERN.matcher(line);
            if (m.find()) {
                String fqn = m.group(2);
                if (fqn.endsWith(".*")) {
                    wildcardPackages.add(fqn.substring(0, fqn.length() - 2));
                } else if (knownAnnotations.contains(fqn)) {
                    String simple = fqn.substring(fqn.lastIndexOf('.') + 1);
                    shortToFqn.put(simple, fqn);
                }
            }
        }
        for (String fqn : knownAnnotations) {
            String packageName = fqn.substring(0, fqn.lastIndexOf('.'));
            if (wildcardPackages.contains(packageName)) {
                shortToFqn.put(fqn.substring(fqn.lastIndexOf('.') + 1), fqn);
            }
        }
        // Second pass: count annotation references by short name on this file's lines.
        boolean inBlockComment = false;
        for (int i = 0; i < lines.size(); i++) {
            CommentStripResult stripped = stripComments(lines.get(i), inBlockComment);
            inBlockComment = stripped.inBlockComment();
            String line = stripped.line();
            for (var entry : shortToFqn.entrySet()) {
                String simple = entry.getKey();
                String fqn = entry.getValue();
                // Match @Simple as a token boundary; tolerate spaces and qualified usages.
                Pattern p = Pattern.compile("@" + Pattern.quote(simple) + "\\b");
                Matcher m = p.matcher(line);
                while (m.find()) {
                    int col = m.start() + 1;
                    addHit(projectRoot, file, totals, locations, fqn, i + 1, col,
                            col + simple.length() + 1);
                }
            }
            for (String fqn : knownAnnotations) {
                Pattern p = Pattern.compile("@" + Pattern.quote(fqn) + "\\b");
                Matcher m = p.matcher(line);
                while (m.find()) {
                    int col = m.start() + 1;
                    addHit(projectRoot, file, totals, locations, fqn, i + 1, col,
                            col + fqn.length() + 1);
                }
            }
        }
    }

    private boolean shouldScan(ProjectModel project, Path file) {
        if (Files.isSymbolicLink(file) && !project.followSymlinks()) {
            return false;
        }
        Path normalized = file.toAbsolutePath().normalize();
        if (!project.followSymlinks() && !normalized.startsWith(project.rootDirectory())) {
            return false;
        }
        Path relative = project.rootDirectory().relativize(normalized);
        String normalizedRelative = relative.toString().replace('\\', '/');
        for (String pattern : project.excludedPathPatterns()) {
            if (globMatch(pattern, normalizedRelative)) {
                return false;
            }
        }
        return true;
    }

    private boolean globMatch(String pattern, String relativePath) {
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                regex.append(".*");
                i++;
            } else if (c == '*') {
                regex.append("[^/]*");
            } else {
                if ("\\.[]{}()+-^$?|".indexOf(c) >= 0) {
                    regex.append('\\');
                }
                regex.append(c);
            }
        }
        return relativePath.matches(regex.toString());
    }

    private Set<String> knownAnnotations() {
        Set<String> known = new LinkedHashSet<>(catalog.knownLegacyAnnotations());
        known.add(AnnotationCatalog.JSPECIFY_NULLABLE);
        known.add(AnnotationCatalog.JSPECIFY_NON_NULL);
        known.add(AnnotationCatalog.JSPECIFY_NULL_MARKED);
        known.add(AnnotationCatalog.JSPECIFY_NULL_UNMARKED);
        return known;
    }

    private void addHit(Path projectRoot,
                        Path file,
                        Map<String, Integer> totals,
                        Map<String, List<Location>> locations,
                        String fqn,
                        int line,
                        int startColumn,
                        int endColumn) {
        Path normalizedFile = file.toAbsolutePath().normalize();
        Path reportPath = normalizedFile.startsWith(projectRoot)
                ? projectRoot.relativize(normalizedFile)
                : normalizedFile;
        totals.merge(fqn, 1, Integer::sum);
        locations.computeIfAbsent(fqn, k -> new ArrayList<>())
                .add(new Location(reportPath, line, startColumn, line, endColumn));
    }

    private CommentStripResult stripComments(String line, boolean inBlockComment) {
        StringBuilder out = new StringBuilder(line.length());
        int i = 0;
        boolean inString = false;
        boolean inChar = false;
        while (i < line.length()) {
            if (inBlockComment) {
                int end = line.indexOf("*/", i);
                if (end < 0) {
                    return new CommentStripResult(out.toString(), true);
                }
                i = end + 2;
                inBlockComment = false;
                continue;
            }

            char c = line.charAt(i);
            char next = i + 1 < line.length() ? line.charAt(i + 1) : '\0';
            if (!inString && !inChar && c == '/' && next == '/') {
                break;
            }
            if (!inString && !inChar && c == '/' && next == '*') {
                inBlockComment = true;
                i += 2;
                continue;
            }
            if (!inChar && c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString && c == '\'' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inChar = !inChar;
            }
            out.append(c);
            i++;
        }
        return new CommentStripResult(out.toString(), inBlockComment);
    }

    private record CommentStripResult(String line, boolean inBlockComment) {}
}
