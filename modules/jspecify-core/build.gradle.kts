val jmhVersion = "1.37"
val jmh by sourceSets.creating {
    java.srcDir("src/jmh/java")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += output + compileClasspath
}

dependencies {
    // No external runtime dependencies for the core model layer; keeps the
    // SPI lightweight and embeddable from CLI, Gradle and Maven plugins.
    "jmhImplementation"("org.openjdk.jmh:jmh-core:$jmhVersion")
    "jmhAnnotationProcessor"("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
}

configurations[jmh.implementationConfigurationName].extendsFrom(configurations.implementation.get())
configurations[jmh.runtimeOnlyConfigurationName].extendsFrom(configurations.runtimeOnly.get())

tasks.named<JavaCompile>(jmh.compileJavaTaskName) {
    options.annotationProcessorPath = configurations[jmh.annotationProcessorConfigurationName]
}

tasks.register<JavaExec>("jmhSmoke") {
    group = "verification"
    description = "Runs a short JMH smoke benchmark for the core scanner."
    dependsOn(jmh.classesTaskName)
    classpath = jmh.runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")
    args(
        "io.github.javamodernizationlabs.jspecify.benchmarks.AnnotationScannerBenchmark.scanSampleProject",
        "-bm", "avgt",
        "-tu", "us",
        "-wi", "0",
        "-i", "1",
        "-r", "10ms",
        "-f", "1",
        "-foe", "true"
    )
}
