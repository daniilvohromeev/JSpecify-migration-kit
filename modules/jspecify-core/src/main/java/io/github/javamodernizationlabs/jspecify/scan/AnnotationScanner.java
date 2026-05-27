package io.github.javamodernizationlabs.jspecify.scan;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;
import io.github.javamodernizationlabs.jspecify.AnnotationInventory;
import io.github.javamodernizationlabs.jspecify.Location;
import io.github.javamodernizationlabs.jspecify.ProjectModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.]+)\\s*;");

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
            try (Stream<Path> stream = Files.walk(root, FileVisitOption.FOLLOW_LINKS)) {
                Iterable<Path> javaFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        ::iterator;
                for (Path file : javaFiles) {
                    files++;
                    scanFile(file, totals, locations);
                }
            }
        }
        return new AnnotationInventory(totals, locations, files);
    }

    void scanFile(Path file,
                  Map<String, Integer> totals,
                  Map<String, List<Location>> locations) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Map<String, String> shortToFqn = new HashMap<>();
        // First pass: resolve imports of known annotations and their short names.
        for (String line : lines) {
            Matcher m = IMPORT_PATTERN.matcher(line);
            if (m.find()) {
                String fqn = m.group(2);
                if (catalog.knownLegacyAnnotations().contains(fqn)
                        || fqn.equals(AnnotationCatalog.JSPECIFY_NULLABLE)
                        || fqn.equals(AnnotationCatalog.JSPECIFY_NON_NULL)
                        || fqn.equals(AnnotationCatalog.JSPECIFY_NULL_MARKED)) {
                    String simple = fqn.substring(fqn.lastIndexOf('.') + 1);
                    shortToFqn.put(simple, fqn);
                }
            }
        }
        if (shortToFqn.isEmpty()) {
            return;
        }
        // Second pass: count annotation references by short name on this file's lines.
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (var entry : shortToFqn.entrySet()) {
                String simple = entry.getKey();
                String fqn = entry.getValue();
                // Match @Simple as a token boundary; tolerate spaces and qualified usages.
                Pattern p = Pattern.compile("@" + Pattern.quote(simple) + "\\b");
                Matcher m = p.matcher(line);
                while (m.find()) {
                    int col = m.start() + 1;
                    totals.merge(fqn, 1, Integer::sum);
                    locations.computeIfAbsent(fqn, k -> new ArrayList<>())
                            .add(new Location(file, i + 1, col, i + 1, col + simple.length() + 1));
                }
            }
        }
    }
}
