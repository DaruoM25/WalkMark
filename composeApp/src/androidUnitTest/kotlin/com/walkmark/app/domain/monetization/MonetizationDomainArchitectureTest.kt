package com.walkmark.app.domain.monetization

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MonetizationDomainArchitectureTest {

    private val moduleRoot: File by lazy {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (!File(dir, "build.gradle.kts").exists() && dir.parentFile != null) {
            dir = dir.parentFile
        }
        dir
    }

    private fun sourceFiles(relativeDir: String): List<File> {
        val dir = File(moduleRoot, relativeDir)
        if (!dir.exists()) return emptyList()
        return dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    private fun assertNoForbiddenImports(files: List<File>, forbidden: List<String>, label: String) {
        files.forEach { file ->
            val text = file.readText()
            forbidden.forEach { needle ->
                assertTrue(
                    !text.contains(needle),
                    "$label must not reference '$needle' -> ${file.name}"
                )
            }
        }
    }

    @Test
    fun monetizationDomainHasNoComposeImports() {
        assertNoForbiddenImports(
            files = sourceFiles("src/commonMain/kotlin/com/walkmark/app/domain/monetization"),
            forbidden = listOf("androidx.compose"),
            label = "domain/monetization"
        )
    }

    @Test
    fun monetizationDomainHasNoRoomImports() {
        assertNoForbiddenImports(
            files = sourceFiles("src/commonMain/kotlin/com/walkmark/app/domain/monetization"),
            forbidden = listOf("androidx.room", "com.walkmark.app.core.database"),
            label = "domain/monetization"
        )
    }

    @Test
    fun monetizationDomainHasNoRevenueCatImports() {
        assertNoForbiddenImports(
            files = sourceFiles("src/commonMain/kotlin/com/walkmark/app/domain/monetization"),
            forbidden = listOf("revenuecat", "RevenueCat", "purchases"),
            label = "domain/monetization"
        )
    }

    @Test
    fun startWalkUseCaseHasNoComposeRoomOrRevenueCatReferences() {
        val file = File(
            moduleRoot,
            "src/commonMain/kotlin/com/walkmark/app/domain/walk/StartWalkUseCase.kt"
        )
        assertTrue(file.exists(), "StartWalkUseCase.kt must exist")

        val text = file.readText()
        listOf("androidx.compose", "androidx.room", "revenuecat", "RevenueCat").forEach {
            assertTrue(!text.contains(it), "StartWalkUseCase must not reference '$it'")
        }
    }

    @Test
    fun startWalkUseCaseDoesNotDependOnWalkRepository() {
        val file = File(
            moduleRoot,
            "src/commonMain/kotlin/com/walkmark/app/domain/walk/StartWalkUseCase.kt"
        )
        assertTrue(file.exists(), "StartWalkUseCase.kt must exist")

        val text = file.readText()
        assertTrue(
            !text.contains("WalkRepository"),
            "StartWalkUseCase must depend on PersistedWalkCount, not WalkRepository"
        )
        assertTrue(
            text.contains("PersistedWalkCount"),
            "StartWalkUseCase must consume the narrow PersistedWalkCount port"
        )
    }

    @Test
    fun monetizationSourceHasNoFakeInCommonMain() {
        val commonMain = sourceFiles("src/commonMain/kotlin/com/walkmark/app")
        val offenders = commonMain.filter { it.name.contains("Fake") && it.readText().isNotEmpty() }

        assertEquals(
            emptyList(),
            offenders.map { it.name },
            "no Fake test double may live in commonMain"
        )
    }

    @Test
    fun productionProviderIsNamedUnavailableAndNotAFake() {
        val file = File(
            moduleRoot,
            "src/commonMain/kotlin/com/walkmark/app/data/monetization/UnavailableSubscriptionManager.kt"
        )
        assertTrue(file.exists(), "UnavailableSubscriptionManager must exist in commonMain")

        val fakeInMain = sourceFiles("src/commonMain/kotlin/com/walkmark/app/data/monetization")
            .filter { it.name.contains("InMemorySubscriptionManager") || it.name.contains("Fake") }

        assertEquals(
            emptyList(),
            fakeInMain.map { it.name },
            "InMemorySubscriptionManager/Fake must not exist in commonMain"
        )
    }

    @Test
    fun presentationLayerHasNoFabricatedStorePrices() {
        val presentation = sourceFiles("src/commonMain/kotlin/com/walkmark/app/presentation")
        presentation.forEach { file ->
            val text = file.readText()
            listOf("2.99", "19.99", "\$2", "\$19").forEach { needle ->
                assertTrue(
                    !text.contains(needle),
                    "runtime UI must not hardcode planned pricing '$needle' -> ${file.name}"
                )
            }
        }
    }

    @Test
    fun noGradleOrToolchainFileWasModifiedByThisFeature() {
        val forbidden = listOf("revenuecat", "RevenueCat", "purchases")
        listOf("build.gradle.kts", "../gradle/libs.versions.toml").forEach { relative ->
            val file = File(moduleRoot, relative)
            if (file.exists()) {
                val text = file.readText()
                forbidden.forEach {
                    assertTrue(!text.contains(it), "$relative must not reference '$it'")
                }
            }
        }
    }
}
