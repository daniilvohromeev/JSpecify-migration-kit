package io.github.javamodernizationlabs.jspecify.coverage;

public record CoverageSummary(
        int publicApiElements,
        int specifiedPublicApiElements,
        int nullMarkedPackages,
        int packagesSeen,
        int ambiguousAnnotations
) {
    public double specifiedRatio() {
        return publicApiElements == 0
                ? 1.0d
                : (double) specifiedPublicApiElements / (double) publicApiElements;
    }

    public double nullMarkedPackageRatio() {
        return packagesSeen == 0
                ? 1.0d
                : (double) nullMarkedPackages / (double) packagesSeen;
    }
}
