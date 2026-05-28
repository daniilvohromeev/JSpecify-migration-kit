plugins {
    application
}

dependencies {
    implementation(project(":jspecify-core"))
    implementation(project(":jspecify-rewrite-recipes"))
    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
}

application {
    mainClass.set("io.github.javamodernizationlabs.jspecify.cli.JspecifyCli")
    applicationName = "jml"
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "jml-cli",
            "Implementation-Version" to project.version.toString()
        )
    }
}
