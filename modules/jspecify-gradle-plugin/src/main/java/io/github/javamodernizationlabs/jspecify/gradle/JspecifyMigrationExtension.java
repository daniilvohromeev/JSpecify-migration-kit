package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class JspecifyMigrationExtension {

    private final MigrationSpec migration;
    private final NullAwaySpec nullaway;
    private final KotlinVerificationSpec kotlinVerification;
    private final ReportsSpec reports;

    @Inject
    public JspecifyMigrationExtension(ObjectFactory objects) {
        this.migration = objects.newInstance(MigrationSpec.class);
        this.nullaway = objects.newInstance(NullAwaySpec.class);
        this.kotlinVerification = objects.newInstance(KotlinVerificationSpec.class);
        this.reports = objects.newInstance(ReportsSpec.class);
    }

    public abstract Property<String> getJspecifyVersion();
    public abstract Property<Boolean> getAddNullMarked();
    public abstract Property<Boolean> getConvertKnownAnnotations();
    public abstract DirectoryProperty getReportsDirectory();

    public MigrationSpec getMigration() {
        return migration;
    }

    public NullAwaySpec getNullaway() {
        return nullaway;
    }

    public KotlinVerificationSpec getKotlinVerification() {
        return kotlinVerification;
    }

    public ReportsSpec getReports() {
        return reports;
    }

    public void migration(Action<? super MigrationSpec> action) {
        action.execute(migration);
    }

    public void nullaway(Action<? super NullAwaySpec> action) {
        action.execute(nullaway);
    }

    public void kotlinVerification(Action<? super KotlinVerificationSpec> action) {
        action.execute(kotlinVerification);
    }

    public void reports(Action<? super ReportsSpec> action) {
        action.execute(reports);
    }

    public abstract static class MigrationSpec {
        public abstract Property<String> getMode();
        public abstract Property<String> getDefaultScope();
        public abstract Property<Boolean> getAddNullMarked();
        public abstract Property<Boolean> getConvertKnownAnnotations();
        public abstract Property<Boolean> getInferFromJavadocs();
    }

    public abstract static class NullAwaySpec {
        public abstract Property<Boolean> getEnabled();
        public abstract Property<String> getMode();
        public abstract ListProperty<String> getAnnotatedPackages();
        public abstract ListProperty<String> getExcludedClasses();
    }

    public abstract static class KotlinVerificationSpec {
        public abstract Property<Boolean> getEnabled();
        public abstract Property<String> getGeneratedSourceSet();
        public abstract Property<Boolean> getFailOnWarnings();
    }

    public abstract static class ReportsSpec {
        public abstract Property<Boolean> getHtmlRequired();
        public abstract Property<Boolean> getSarifRequired();
        public abstract Property<Boolean> getMarkdownRequired();
        public abstract Property<Boolean> getJsonRequired();
        public abstract Property<Boolean> getJunitXmlRequired();
    }
}
