plugins {
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":jspecify-core"))
    implementation(project(":jspecify-rewrite-recipes"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("jspecifyMigration") {
            id = "io.github.javamodernizationlabs.jspecify-migration"
            implementationClass =
                "io.github.javamodernizationlabs.jspecify.gradle.JspecifyMigrationPlugin"
            displayName = "JSpecify Migration Kit"
            description = "Plan and apply migrations to JSpecify null-safety annotations."
        }
    }
}
