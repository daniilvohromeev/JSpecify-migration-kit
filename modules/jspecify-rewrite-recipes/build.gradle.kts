dependencies {
    implementation(project(":jspecify-core"))
    implementation(platform("org.openrewrite:rewrite-bom:8.41.1"))
    implementation("org.openrewrite:rewrite-core")
    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-maven")
    implementation("org.openrewrite:rewrite-gradle")
    implementation("org.openrewrite:rewrite-yaml")

    testImplementation(platform("org.openrewrite:rewrite-bom:8.41.1"))
    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.openrewrite:rewrite-java-21")
}
