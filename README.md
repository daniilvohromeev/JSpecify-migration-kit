# JSpecify Migration Kit

Tooling for migrating Java projects to JSpecify nullness annotations.

## Quick start

```bash
./gradlew :jspecify-cli:installDist
modules/jspecify-cli/build/install/jml/bin/jml jspecify plan --project .
modules/jspecify-cli/build/install/jml/bin/jml jspecify rewrite --recipe convert-known-annotations --dry-run
```

Use `--apply` only after reviewing the generated rewrite report:

```bash
modules/jspecify-cli/build/install/jml/bin/jml jspecify rewrite \
  --recipe add-dependency,convert-known-annotations \
  --apply
```

## CLI

```bash
jml jspecify plan --project .
jml jspecify coverage --scope public-api --format html,json
jml jspecify verify-kotlin --generate-samples --compile
jml jspecify nullaway-config --mode warn --apply
jml jspecify explain jspecify.public-api-unspecified
```

CI baseline flow:

```bash
jml jspecify plan --project . --baseline-write config/jspecify-baseline.json
jml jspecify plan --project . --baseline config/jspecify-baseline.json --fail-on high
```

## Gradle

```kotlin
plugins {
    id("io.github.javamodernizationlabs.jspecify-migration") version "0.1.0"
}

jspecifyMigration {
    jspecifyVersion.set("1.0.0")

    migration {
        mode.set("incremental")
        defaultScope.set("public-api")
        convertKnownAnnotations.set(true)
        addNullMarked.set(false)
    }

    nullaway {
        enabled.set(true)
        mode.set("warn")
        annotatedPackages.set(listOf("com.acme"))
    }

    kotlinVerification {
        enabled.set(true)
        generatedSourceSet.set("jspecifyKotlinVerification")
    }
}
```

Main tasks:

```bash
./gradlew jspecifyPlan
./gradlew jspecifyRewriteDryRun
./gradlew jspecifyRewriteApply
./gradlew jspecifyCoverage
./gradlew jspecifyNullAwayCheck
./gradlew jspecifyVerifyKotlin
```

## Maven

```xml
<plugin>
  <groupId>io.github.javamodernizationlabs</groupId>
  <artifactId>jspecify-migration-maven-plugin</artifactId>
  <version>0.1.0</version>
  <configuration>
    <jspecifyVersion>1.0.0</jspecifyVersion>
    <migrationMode>incremental</migrationMode>
    <convertKnownAnnotations>true</convertKnownAnnotations>
    <addNullMarked>false</addNullMarked>
  </configuration>
</plugin>
```

Goals:

```bash
mvn jspecify-migration:plan
mvn jspecify-migration:rewrite-dry-run
mvn jspecify-migration:rewrite-apply
mvn jspecify-migration:coverage
mvn jspecify-migration:nullaway-check
mvn jspecify-migration:verify-kotlin
```

## Before and After

Legacy input:

```java
package com.acme;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserApi {
    public @NotNull String name() { return ""; }
    public @Nullable String nickname() { return null; }
}
```

After `convert-known-annotations`:

```java
package com.acme;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class UserApi {
    public @NonNull String name() { return ""; }
    public @Nullable String nickname() { return null; }
}
```

After package policy review, prefer package-level `@NullMarked`:

```java
@NullMarked
package com.acme;

import org.jspecify.annotations.NullMarked;
```

The Kotlin verifier then generates typed assertions:

```kotlin
fun verifyUserApi(api: com.acme.UserApi) {
    val nameValue: String = api.name()
    val nicknameValue: String? = api.nickname()
}
```

Unmarked public API is reported as `KOTLIN_PLATFORM_TYPE_LEAK` until the package or
return type has an explicit nullness contract.

## Recipes

The OpenRewrite catalog is published from `jspecify-rewrite-recipes`.

```kotlin
plugins {
    id("org.openrewrite.rewrite") version "latest.release"
}

rewrite {
    activeRecipe("io.github.jml.jspecify.Migrate")
}

dependencies {
    rewrite("io.github.javamodernizationlabs:jspecify-migration-rewrite-recipes:0.1.0")
}
```

Available presets:

- `io.github.jml.jspecify.SpringPreset`
- `io.github.jml.jspecify.ReactorPreset`
- `io.github.jml.jspecify.MicrometerPreset`

## GitHub Actions

This repository includes a composite action:

```yaml
- uses: java-modernization-labs/jml-action@v0.1.0
  with:
    tools: jspecify
    fail-on: high
    sarif-upload: true
```

When used from this repository, the local path is:

```yaml
- uses: ./.github/actions/jml-analyze
  with:
    tools: jspecify
    sarif-upload: true
```

## Release Checks

```bash
./gradlew build
./gradlew publishToMavenLocal
./gradlew releaseQualityGate
```

`releaseQualityGate` verifies OSS metadata, reproducible archive settings, local
quality gates and GitHub security scanning wiring.

## Modules

- `jspecify-core` - shared issue, report, config, inventory and rewrite models.
- `jspecify-rewrite-recipes` - OpenRewrite recipe catalog.
- `jspecify-cli` - `jml jspecify ...` command line surface.
- `jspecify-gradle-plugin` - Gradle plugin `io.github.javamodernizationlabs.jspecify-migration`.
- `jspecify-maven-plugin` - Maven goals for plan, rewrite, coverage, NullAway and Kotlin verification.

## License

Apache License 2.0. See [LICENSE](LICENSE).
