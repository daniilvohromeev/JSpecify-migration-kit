plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.1.1"
}

dependencies {
    implementation(project(":jspecify-core"))
    implementation(project(":jspecify-rewrite-recipes"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website.set("https://github.com/javamodernizationlabs/JSpecify-migration-kit")
    vcsUrl.set("https://github.com/javamodernizationlabs/JSpecify-migration-kit")
    plugins {
        create("jspecifyMigration") {
            id = "io.github.javamodernizationlabs.jspecify-migration"
            implementationClass =
                "io.github.javamodernizationlabs.jspecify.gradle.JspecifyMigrationPlugin"
            displayName = "JSpecify Migration Kit"
            description = "Plan and apply migrations to JSpecify null-safety annotations."
            tags.set(listOf("jspecify", "nullness", "migration", "openrewrite"))
        }
    }
}
