// The Maven plugin Mojos are implemented as plain Java classes that depend on
// Maven Plugin API. A real Maven plugin packaging step (plugin descriptor +
// jar layout) is intentionally out of scope for the v0.1 build wiring — the
// classes are publishable as a regular JAR and a follow-up task will wire the
// `maven-plugin-plugin` equivalent via the maven-publish toolchain.
dependencies {
    implementation(project(":jspecify-core"))
    implementation(project(":jspecify-rewrite-recipes"))
    compileOnly("org.apache.maven:maven-plugin-api:3.9.9")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.13.1")
    compileOnly("org.apache.maven:maven-core:3.9.9")
    compileOnly("org.apache.maven:maven-model:3.9.9")

    testImplementation("org.apache.maven:maven-plugin-api:3.9.9")
    testImplementation("org.apache.maven.plugin-tools:maven-plugin-annotations:3.13.1")
    testImplementation("org.apache.maven:maven-core:3.9.9")
    testImplementation("org.apache.maven:maven-model:3.9.9")
}
