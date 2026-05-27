package io.github.javamodernizationlabs.jspecify.rewrite;

import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Adds package-level JSpecify defaults only where the package can be updated
 * without overriding an existing non-JSpecify default annotation.
 */
public class AddNullMarkedToPackage
        extends ScanningRecipe<AddNullMarkedToPackage.PackageIndex> {

    private static final Set<String> CONFLICTING_DEFAULT_ANNOTATIONS = Set.of(
            "ParametersAreNonnullByDefault",
            "NonNullApi",
            "NonNullFields",
            "TypeQualifierDefault",
            "DefaultAnnotation",
            "DefaultQualifier");

    @Override
    public String getDisplayName() {
        return "Add package-level JSpecify NullMarked";
    }

    @Override
    public String getDescription() {
        return "Creates or updates package-info.java with package-level @NullMarked, "
                + "while skipping generated sources and packages with conflicting default "
                + "nullness annotations.";
    }

    @Override
    public PackageIndex getInitialValue(ExecutionContext ctx) {
        return new PackageIndex();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(PackageIndex packageIndex) {
        return new JavaIsoVisitor<>() {
            @Override
            public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu,
                                                          ExecutionContext ctx) {
                J.Package packageDeclaration = cu.getPackageDeclaration();
                if (packageDeclaration == null || isGenerated(cu.getSourcePath())) {
                    return cu;
                }

                String packageName = packageDeclaration.getPackageName();
                Path packageInfoPath = packageInfoPath(cu.getSourcePath(), packageName);
                PackageState state = packageIndex.packages.computeIfAbsent(
                        packageInfoPath,
                        ignored -> new PackageState(packageName, packageInfoPath));

                if (cu.getSourcePath().getFileName().toString().equals("package-info.java")) {
                    state.packageInfoExists = true;
                    state.nullMarked = state.nullMarked || hasAnnotation(packageDeclaration,
                            "NullMarked");
                    state.conflictingDefaults = state.conflictingDefaults
                            || hasConflictingDefault(packageDeclaration);
                }

                return cu;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(PackageIndex packageIndex,
                                                     ExecutionContext ctx) {
        return packageIndex.packages.values().stream()
                .filter(PackageState::shouldCreatePackageInfo)
                .map(AddNullMarkedToPackage::createPackageInfo)
                .toList();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(PackageIndex packageIndex) {
        return new JavaIsoVisitor<>() {
            @Override
            public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu,
                                                          ExecutionContext ctx) {
                J.CompilationUnit updated = super.visitCompilationUnit(cu, ctx);
                PackageState state = packageIndex.packages.get(updated.getSourcePath());
                if (state == null || !state.shouldUpdatePackageInfo()) {
                    return updated;
                }

                J.Package packageDeclaration = updated.getPackageDeclaration();
                if (packageDeclaration == null || hasAnnotation(packageDeclaration,
                        "NullMarked")) {
                    return updated;
                }

                maybeAddImport("org.jspecify.annotations.NullMarked", false);
                List<J.Annotation> annotations = new ArrayList<>(
                        packageDeclaration.getAnnotations());
                J.Annotation annotation = nullMarkedAnnotation(state.packageName)
                        .withPrefix(annotations.isEmpty()
                                ? Space.EMPTY
                                : Space.format("\n"));
                annotations.add(annotation);
                return updated.withPackageDeclaration(
                        packageDeclaration.withAnnotations(annotations)
                                .withPrefix(Space.format("\n")));
            }
        };
    }

    private static boolean hasAnnotation(J.Package packageDeclaration,
                                         String simpleName) {
        return packageDeclaration.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getSimpleName().equals(simpleName));
    }

    private static boolean hasConflictingDefault(J.Package packageDeclaration) {
        return packageDeclaration.getAnnotations().stream()
                .map(J.Annotation::getSimpleName)
                .anyMatch(CONFLICTING_DEFAULT_ANNOTATIONS::contains);
    }

    private static boolean isGenerated(Path sourcePath) {
        String normalized = sourcePath.toString().replace('\\', '/')
                .toLowerCase(Locale.ROOT);
        return normalized.contains("/generated/")
                || normalized.startsWith("generated/")
                || normalized.contains("/target/generated-sources/")
                || normalized.startsWith("target/generated-sources/")
                || normalized.contains("/build/generated/")
                || normalized.startsWith("build/generated/");
    }

    private static Path packageInfoPath(Path sourcePath, String packageName) {
        Path packagePath = Paths.get("", packageName.split("\\."));
        Path parent = sourcePath.getParent();
        if (parent == null || !parent.endsWith(packagePath)) {
            return packagePath.resolve("package-info.java");
        }

        int sourceRootNameCount = parent.getNameCount() - packagePath.getNameCount();
        Path sourceRoot = sourceRootNameCount == 0
                ? Paths.get("")
                : parent.subpath(0, sourceRootNameCount);
        return sourceRoot.resolve(packagePath).resolve("package-info.java");
    }

    private static String packageInfoSource(String packageName) {
        return "@NullMarked\n"
                + "package " + packageName + ";\n\n"
                + "import org.jspecify.annotations.NullMarked;\n";
    }

    private static SourceFile createPackageInfo(PackageState state) {
        SourceFile sourceFile = JavaParser.fromJavaVersion()
                .build()
                .parse(packageInfoSource(state.packageName))
                .findFirst()
                .orElseThrow();
        return sourceFile.withSourcePath(state.packageInfoPath);
    }

    private static J.Annotation nullMarkedAnnotation(String packageName) {
        return JavaParser.fromJavaVersion()
                .build()
                .parse(packageInfoSource(packageName))
                .filter(J.CompilationUnit.class::isInstance)
                .map(J.CompilationUnit.class::cast)
                .map(J.CompilationUnit::getPackageDeclaration)
                .map(pkg -> pkg.getAnnotations().get(0))
                .map(annotation -> annotation.withPrefix(Space.EMPTY))
                .findFirst()
                .orElseThrow();
    }

    static final class PackageIndex {
        private final Map<Path, PackageState> packages = new LinkedHashMap<>();
    }

    static final class PackageState {
        private final String packageName;
        private final Path packageInfoPath;
        private boolean packageInfoExists;
        private boolean nullMarked;
        private boolean conflictingDefaults;

        private PackageState(String packageName, Path packageInfoPath) {
            this.packageName = packageName;
            this.packageInfoPath = packageInfoPath;
        }

        private boolean shouldCreatePackageInfo() {
            return !packageInfoExists && !nullMarked && !conflictingDefaults;
        }

        private boolean shouldUpdatePackageInfo() {
            return packageInfoExists && !nullMarked && !conflictingDefaults;
        }
    }
}
