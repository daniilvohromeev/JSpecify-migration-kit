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
