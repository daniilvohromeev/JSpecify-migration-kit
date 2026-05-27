plugins {
    java
    `maven-publish`
}

val publishedArtifactIds = mapOf(
    "jspecify-core" to "jspecify-migration-core",
    "jspecify-rewrite-recipes" to "jspecify-migration-rewrite-recipes",
    "jspecify-cli" to "jml-cli",
    "jspecify-gradle-plugin" to "jspecify-migration-gradle-plugin",
    "jspecify-maven-plugin" to "jspecify-migration-maven-plugin",
)

allprojects {
    group = "io.github.javamodernizationlabs"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    val publishedArtifactId = publishedArtifactIds[name] ?: name

    extensions.configure<BasePluginExtension> {
        archivesName.set(publishedArtifactId)
    }

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:5.11.3"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }

    tasks.withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    extensions.configure<org.gradle.api.publish.PublishingExtension> {
        publications {
            create("mavenJava", org.gradle.api.publish.maven.MavenPublication::class.java) {
                from(components["java"])
                artifactId = publishedArtifactId
                pom {
                    name.set(publishedArtifactId)
                    description.set("JSpecify Migration Kit tooling for Java nullness migration.")
                    url.set("https://github.com/daniilvohromeev/JSpecify-migration-kit")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/daniilvohromeev/JSpecify-migration-kit.git")
                        developerConnection.set("scm:git:ssh://github.com/daniilvohromeev/JSpecify-migration-kit.git")
                        url.set("https://github.com/daniilvohromeev/JSpecify-migration-kit")
                    }
                    developers {
                        developer {
                            id.set("java-modernization-labs")
                            name.set("Java Modernization Labs contributors")
                        }
                    }
                }
            }
        }
    }
}

tasks.register("licenseCheck") {
    group = "verification"
    description = "Checks OSS metadata required for release publication."
    inputs.files("LICENSE", "NOTICE", "README.md", "SECURITY.md", "CONTRIBUTING.md")
    doLast {
        val required = listOf("LICENSE", "NOTICE", "README.md", "SECURITY.md", "CONTRIBUTING.md")
        val missing = required.filterNot { layout.projectDirectory.file(it).asFile.isFile }
        check(missing.isEmpty()) {
            "Missing required OSS metadata files: ${missing.joinToString(", ")}"
        }
        check(layout.projectDirectory.file("LICENSE").asFile.readText()
            .contains("Apache License")) {
            "LICENSE must contain Apache License text."
        }
        check(layout.projectDirectory.file("NOTICE").asFile.readText()
            .contains("JSpecify Migration Kit")) {
            "NOTICE must identify the project."
        }
    }
}

tasks.register("reproducibleBuildCheck") {
    group = "verification"
    description = "Verifies archive tasks use reproducible ordering and timestamps."
    dependsOn(subprojects.map { it.tasks.named("jar") })
    doLast {
        subprojects.forEach { project ->
            project.tasks.withType<Jar>().forEach { jar ->
                check(!jar.isPreserveFileTimestamps) {
                    "${project.path}:${jar.name} preserves file timestamps."
                }
                check(jar.isReproducibleFileOrder) {
                    "${project.path}:${jar.name} does not use reproducible file order."
                }
            }
        }
    }
}

tasks.register("dependencyVulnerabilityCheck") {
    group = "verification"
    description = "Ensures dependency security scanning is wired in GitHub Actions."
    inputs.file(".github/workflows/security.yml")
    doLast {
        val workflow = layout.projectDirectory.file(".github/workflows/security.yml").asFile
        check(workflow.isFile) {
            "Missing .github/workflows/security.yml for dependency and CodeQL scanning."
        }
        val text = workflow.readText()
        check(text.contains("actions/dependency-review-action")) {
            "security.yml must run GitHub dependency review."
        }
        check(text.contains("github/codeql-action/init")) {
            "security.yml must initialize CodeQL."
        }
    }
}

tasks.register("jmhSmoke") {
    group = "verification"
    description = "Placeholder smoke gate for future JMH benchmarks; verifies benchmark scope is explicit."
    inputs.file("build.gradle.kts")
    doLast {
        check(layout.projectDirectory.file("build.gradle.kts").asFile.readText()
            .contains("version = \"0.1.0-SNAPSHOT\"")) {
            "Project versioning must be defined before benchmark publication."
        }
    }
}

tasks.register("releaseQualityGate") {
    group = "verification"
    description = "Runs the local release quality gates documented for JSpecify Migration Kit."
    dependsOn("build", "licenseCheck", "reproducibleBuildCheck",
        "dependencyVulnerabilityCheck", "jmhSmoke")
}
