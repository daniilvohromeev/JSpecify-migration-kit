package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public abstract class JspecifyMigrationExtension {

    public abstract Property<String> getJspecifyVersion();
    public abstract Property<Boolean> getAddNullMarked();
    public abstract Property<Boolean> getConvertKnownAnnotations();
    public abstract DirectoryProperty getReportsDirectory();
}
