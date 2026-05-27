package io.github.javamodernizationlabs.jspecify.coverage;

public record CoverageSummary(
        int publicApiElements,
        int specifiedPublicApiElements,
        int nullMarkedPackages,
        int packagesSeen,
        int ambiguousAnnotations,
        int publicMethods,
        int returnNullnessSpecified,
        int publicParameters,
        int parameterNullnessSpecified,
        int genericTypeUses,
        int genericTypeUseNullnessSpecified,
        int kotlinInteropWarnings
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

    public double returnNullnessRatio() {
        return publicMethods == 0
                ? 1.0d
                : (double) returnNullnessSpecified / (double) publicMethods;
    }

    public double parameterNullnessRatio() {
        return publicParameters == 0
                ? 1.0d
                : (double) parameterNullnessSpecified / (double) publicParameters;
    }

    public double genericTypeUseRatio() {
        return genericTypeUses == 0
                ? 1.0d
                : (double) genericTypeUseNullnessSpecified / (double) genericTypeUses;
    }
}
