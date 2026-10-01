package com.walkmark.app.domain.cloud

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertTrue

/**
 * Source-level architecture guard for the US-009B1 cloud persistence contracts.
 *
 * Multiplatform common tests cannot read the filesystem and kotlin-reflect is not
 * applied, so these forbidden-reference rules are asserted here, following the
 * existing MonetizationDomainArchitectureTest precedent.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CloudDomainArchitectureTest {

    private val moduleRoot: File by lazy {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (!File(dir, "build.gradle.kts").exists() && dir.parentFile != null) {
            dir = dir.parentFile
        }
        dir
    }

    private val cloudDir = "src/commonMain/kotlin/com/walkmark/app/domain/cloud"

    private fun sourceFiles(relativeDir: String): List<File> {
        val dir = File(moduleRoot, relativeDir)
        if (!dir.exists()) return emptyList()
        return dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    /**
     * Comments are removed before matching so that documentation may describe a
     * forbidden concept without tripping the rule it documents.
     */
    private fun declarationsOnly(file: File): String =
        file.readText()
            .replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), " ")
            .lines()
            .joinToString("\n") { it.substringBefore("//") }

    private fun assertCloudDomainHasNoForbiddenReferences(forbidden: List<String>, label: String) {
        val files = sourceFiles(cloudDir)
        assertTrue(files.isNotEmpty(), "domain/cloud must exist in commonMain")

        files.forEach { file ->
            val declarations = declarationsOnly(file)
            forbidden.forEach { needle ->
                assertTrue(
                    !declarations.contains(needle),
                    "$label must not reference '$needle' -> ${file.name}"
                )
            }
        }
    }

    @Test
    fun cloudDomainContractFilesArePresent() {
        val expected = listOf(
            "CloudOwnerId.kt",
            "CloudEntityId.kt",
            "CloudResult.kt",
            "CloudRemoteModels.kt",
            "CloudWalkStore.kt",
            "CloudRouteStore.kt",
            "CloudNoteStore.kt",
            "CloudPhotoMetadataStore.kt"
        )

        val present = sourceFiles(cloudDir).map { it.name }

        expected.forEach { name ->
            assertTrue(present.contains(name), "domain/cloud must contain $name -> $present")
        }
    }

    @Test
    fun cloudDomainHasNoAuthenticationDependencies() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf("domain.auth", "AuthUser", "AuthRepository", "AuthSessionState", "spike", "Spike"),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudDomainHasNoSyncScopeDuplication() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf(
                "domain.sync",
                "SyncCoordinator",
                "SyncQueueRepository",
                "SyncOperation",
                "SyncStatus",
                "SyncResult",
                "SyncFailure",
                "SyncEntityType"
            ),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudDomainHasNoProviderSdkOrTransportTypes() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf("io.ktor", "io.supabase", "jan-tennert", "supabase", "HttpClient", "Postgrest"),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudDomainHasNoRoomOrPersistenceImplementationTypes() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf("androidx.room", "core.database", "@Entity", "@Dao", "WalkMarkDatabase"),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudDomainHasNoComposeOrUiTypes() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf("androidx.compose", "Composable"),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudDomainExposesNoFilesystemLocator() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf("relativePath", "absolutePath", "Uri", "uri", "path"),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudDomainCarriesNoTombstoneOrDeletionMetadata() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf("deletedAt", "tombstone", "isDeleted", "deletedFlag"),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudDomainCarriesNoQueueRetryOrConflictResolutionState() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf(
                "attemptCount",
                "lastAttempt",
                "retryCount",
                "pendingOperation",
                "remoteVersion",
                "conflictState",
                "syncState",
                "dirty"
            ),
            label = "domain/cloud"
        )
    }

    @Test
    fun cloudFailureModelCarriesNoThrowableOrFreeTextProviderMessage() {
        val file = File(moduleRoot, "$cloudDir/CloudResult.kt")
        assertTrue(file.exists(), "CloudResult.kt must exist in commonMain")

        val declarations = declarationsOnly(file)
        listOf("Throwable", "message", "reason", "cause", "stackTrace").forEach { needle ->
            assertTrue(
                !declarations.contains(needle, ignoreCase = true),
                "CloudFailure must not carry '$needle' -> $declarations"
            )
        }
    }

    @Test
    fun cloudDomainHasNoPhotoBinaryUploadPort() {
        assertCloudDomainHasNoForbiddenReferences(
            forbidden = listOf("uploadPhoto", "PhotoBinary", "putObject", "UploadPort", "uploadBinary"),
            label = "domain/cloud"
        )
    }
}