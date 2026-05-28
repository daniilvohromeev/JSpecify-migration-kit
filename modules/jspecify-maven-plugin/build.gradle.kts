import org.apache.tools.ant.filters.ReplaceTokens

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

tasks.named<Copy>("processResources") {
    inputs.property("projectVersion", project.version.toString())
    filesMatching("META-INF/maven/plugin.xml") {
        filter<ReplaceTokens>("tokens" to mapOf(
            "PROJECT_VERSION" to project.version.toString()))
    }
}
