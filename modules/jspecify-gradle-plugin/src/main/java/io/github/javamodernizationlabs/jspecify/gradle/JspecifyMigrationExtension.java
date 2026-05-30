package io.github.javamodernizationlabs.jspecify.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Gradle DSL extension that configures the JSpecify Migration Kit for a project.
 *
 * <p>This extension is registered under the name {@code jspecifyMigration} by
 * {@link JspecifyMigrationPlugin}. It exposes top-level properties as well as nested
 * configuration blocks for migration behaviour ({@link MigrationSpec}), NullAway
 * verification ({@link NullAwaySpec}), Kotlin interop verification
 * ({@link KotlinVerificationSpec}) and report formats ({@link ReportsSpec}).</p>
 */
public abstract class JspecifyMigrationExtension {

    private final MigrationSpec migration;
    private final NullAwaySpec nullaway;
    private final KotlinVerificationSpec kotlinVerification;
    private final ReportsSpec reports;

    /**
     * Creates the extension and instantiates its nested configuration blocks.
     *
     * <p>Gradle invokes this constructor reflectively, injecting the {@link ObjectFactory}
     * used to create the managed nested specs.</p>
     *
     * @param objects the Gradle object factory used to create the nested specifications
     */
    @Inject
    public JspecifyMigrationExtension(ObjectFactory objects) {
        this.migration = objects.newInstance(MigrationSpec.class);
        this.nullaway = objects.newInstance(NullAwaySpec.class);
        this.kotlinVerification = objects.newInstance(KotlinVerificationSpec.class);
        this.reports = objects.newInstance(ReportsSpec.class);
    }

    /**
     * The JSpecify dependency version to align migrations against.
     *
     * @return a {@code Property<String>} holding the JSpecify version
     */
    public abstract Property<String> getJspecifyVersion();

    /**
     * Whether to add {@code @NullMarked} annotations during migration.
     *
     * @return a {@code Property<Boolean>} that is {@code true} when {@code @NullMarked} should be added
     */
    public abstract Property<Boolean> getAddNullMarked();

    /**
     * Whether to convert known legacy nullness annotations to JSpecify equivalents.
     *
     * @return a {@code Property<Boolean>} that is {@code true} when known annotations should be converted
     */
    public abstract Property<Boolean> getConvertKnownAnnotations();

    /**
     * The directory into which JSpecify reports are written.
     *
     * @return a {@link DirectoryProperty} pointing at the reports output directory
     */
    public abstract DirectoryProperty getReportsDirectory();

    /**
     * Returns the migration configuration block.
     *
     * @return the {@link MigrationSpec} for this extension
     */
    public MigrationSpec getMigration() {
        return migration;
    }

    /**
     * Returns the NullAway verification configuration block.
     *
     * @return the {@link NullAwaySpec} for this extension
     */
    public NullAwaySpec getNullaway() {
        return nullaway;
    }

    /**
     * Returns the Kotlin interop verification configuration block.
     *
     * @return the {@link KotlinVerificationSpec} for this extension
     */
    public KotlinVerificationSpec getKotlinVerification() {
        return kotlinVerification;
    }

    /**
     * Returns the report-formats configuration block.
     *
     * @return the {@link ReportsSpec} for this extension
     */
    public ReportsSpec getReports() {
        return reports;
    }

    /**
     * Configures the migration block using the supplied action.
     *
     * @param action the action applied to the {@link MigrationSpec}
     */
    public void migration(Action<? super MigrationSpec> action) {
        action.execute(migration);
    }

    /**
     * Configures the NullAway block using the supplied action.
     *
     * @param action the action applied to the {@link NullAwaySpec}
     */
    public void nullaway(Action<? super NullAwaySpec> action) {
        action.execute(nullaway);
    }

    /**
     * Configures the Kotlin interop verification block using the supplied action.
     *
     * @param action the action applied to the {@link KotlinVerificationSpec}
     */
    public void kotlinVerification(Action<? super KotlinVerificationSpec> action) {
        action.execute(kotlinVerification);
    }

    /**
     * Configures the report-formats block using the supplied action.
     *
     * @param action the action applied to the {@link ReportsSpec}
     */
    public void reports(Action<? super ReportsSpec> action) {
        action.execute(reports);
    }

    /**
     * Configuration block controlling how the JSpecify migration is performed.
     *
     * <p>Instances are created and managed by Gradle through the owning
     * {@link JspecifyMigrationExtension}.</p>
     */
    public abstract static class MigrationSpec {

        /**
         * Creates a migration specification.
         *
         * <p>Gradle instantiates this type reflectively; this constructor exists only to
         * provide documented public API.</p>
         */
        public MigrationSpec() {
        }

        /**
         * The migration mode, for example {@code incremental}.
         *
         * @return a {@code Property<String>} holding the migration mode
         */
        public abstract Property<String> getMode();

        /**
         * The default scope to migrate, for example {@code public-api}.
         *
         * @return a {@code Property<String>} holding the default migration scope
         */
        public abstract Property<String> getDefaultScope();

        /**
         * Whether to add {@code @NullMarked} annotations during migration.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when {@code @NullMarked} should be added
         */
        public abstract Property<Boolean> getAddNullMarked();

        /**
         * Whether to convert known legacy nullness annotations to JSpecify equivalents.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when known annotations should be converted
         */
        public abstract Property<Boolean> getConvertKnownAnnotations();

        /**
         * Whether to infer nullness from Javadoc comments.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when nullness should be inferred from Javadocs
         */
        public abstract Property<Boolean> getInferFromJavadocs();
    }

    /**
     * Configuration block controlling NullAway/Error Prone verification.
     *
     * <p>Instances are created and managed by Gradle through the owning
     * {@link JspecifyMigrationExtension}.</p>
     */
    public abstract static class NullAwaySpec {

        /**
         * Creates a NullAway specification.
         *
         * <p>Gradle instantiates this type reflectively; this constructor exists only to
         * provide documented public API.</p>
         */
        public NullAwaySpec() {
        }

        /**
         * Whether the NullAway verification profile is enabled.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when NullAway verification is enabled
         */
        public abstract Property<Boolean> getEnabled();

        /**
         * The NullAway severity mode, for example {@code warn} or {@code error}.
         *
         * @return a {@code Property<String>} holding the NullAway mode
         */
        public abstract Property<String> getMode();

        /**
         * The packages NullAway should treat as annotated.
         *
         * @return a {@code ListProperty<String>} of annotated package names
         */
        public abstract ListProperty<String> getAnnotatedPackages();

        /**
         * The classes NullAway should exclude from analysis.
         *
         * @return a {@code ListProperty<String>} of excluded class names
         */
        public abstract ListProperty<String> getExcludedClasses();
    }

    /**
     * Configuration block controlling Kotlin interoperability verification.
     *
     * <p>Instances are created and managed by Gradle through the owning
     * {@link JspecifyMigrationExtension}.</p>
     */
    public abstract static class KotlinVerificationSpec {

        /**
         * Creates a Kotlin verification specification.
         *
         * <p>Gradle instantiates this type reflectively; this constructor exists only to
         * provide documented public API.</p>
         */
        public KotlinVerificationSpec() {
        }

        /**
         * Whether Kotlin interop verification is enabled.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when Kotlin verification is enabled
         */
        public abstract Property<Boolean> getEnabled();

        /**
         * The name of the generated source set used for verification samples.
         *
         * @return a {@code Property<String>} holding the generated source set name
         */
        public abstract Property<String> getGeneratedSourceSet();

        /**
         * Whether to compile the generated Kotlin verification samples.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when samples should be compiled
         */
        public abstract Property<Boolean> getCompileSamples();

        /**
         * Whether verification should fail the build when warnings are reported.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when warnings should fail the build
         */
        public abstract Property<Boolean> getFailOnWarnings();
    }

    /**
     * Configuration block controlling which report formats are produced.
     *
     * <p>Instances are created and managed by Gradle through the owning
     * {@link JspecifyMigrationExtension}.</p>
     */
    public abstract static class ReportsSpec {

        /**
         * Creates a reports specification.
         *
         * <p>Gradle instantiates this type reflectively; this constructor exists only to
         * provide documented public API.</p>
         */
        public ReportsSpec() {
        }

        /**
         * Whether an HTML report is required.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when the HTML report is required
         */
        public abstract Property<Boolean> getHtmlRequired();

        /**
         * Whether a SARIF report is required.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when the SARIF report is required
         */
        public abstract Property<Boolean> getSarifRequired();

        /**
         * Whether a Markdown report is required.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when the Markdown report is required
         */
        public abstract Property<Boolean> getMarkdownRequired();

        /**
         * Whether a JSON report is required.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when the JSON report is required
         */
        public abstract Property<Boolean> getJsonRequired();

        /**
         * Whether a JUnit XML report is required.
         *
         * @return a {@code Property<Boolean>} that is {@code true} when the JUnit XML report is required
         */
        public abstract Property<Boolean> getJunitXmlRequired();
    }
}
