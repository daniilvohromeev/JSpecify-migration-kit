rootProject.name = "jspecify-migration-kit"

include(
    "jspecify-core",
    "jspecify-rewrite-recipes",
    "jspecify-cli",
    "jspecify-gradle-plugin",
    "jspecify-maven-plugin",
)

for (sub in rootProject.children) {
    sub.projectDir = file("modules/${sub.name}")
}
