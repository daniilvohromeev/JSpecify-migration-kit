# JSpecify Migration Kit

Tooling for migrating Java projects to JSpecify nullness annotations.

## Quick start

```bash
./gradlew :jspecify-cli:installDist
modules/jspecify-cli/build/install/jml/bin/jml jspecify plan --project .
modules/jspecify-cli/build/install/jml/bin/jml jspecify rewrite --recipe convert-known-annotations --dry-run
```

## Modules

- `jspecify-core` - shared issue, report, config, inventory and rewrite models.
- `jspecify-rewrite-recipes` - OpenRewrite recipe catalog.
- `jspecify-cli` - `jml jspecify ...` command line surface.
- `jspecify-gradle-plugin` - Gradle plugin `io.github.javamodernizationlabs.jspecify-migration`.
- `jspecify-maven-plugin` - Maven goals for plan, rewrite, coverage, NullAway and Kotlin verification.

## License

Apache License 2.0. See [LICENSE](LICENSE).
