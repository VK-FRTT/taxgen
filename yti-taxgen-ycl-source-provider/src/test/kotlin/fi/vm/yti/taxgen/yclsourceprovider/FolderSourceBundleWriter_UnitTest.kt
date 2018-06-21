package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.yclsourceprovider.folder.FolderSourceBundleWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

@DisplayName("when bundle contents are written to folder")
internal class FolderSourceBundleWriter_UnitTest : SourceBundle_UnitTestBase() {

    @Nested
    @DisplayName("which is empty")
    inner class EmptyTargetFolder {

        private lateinit var targetFolderPath: Path

        @BeforeEach
        fun init() {
            targetFolderPath = Files.createTempDirectory("foldersourcebundlewriter_blank_unittest")

            val sourceBundle = FixedSourceBundle()

            FolderSourceBundleWriter(
                baseFolderPath = targetFolderPath,
                sourceBundle = sourceBundle,
                forceOverwrite = false
            ).use {
                it.write()
            }
        }

        @AfterEach
        fun teardown() {
            Files
                .walk(targetFolderPath)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }

        @Test
        fun `Should have bundleinfo @ root`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "bundle_info.json",
                expectedMarker = "fixed_source_bundle"
            )
        }

        @Test
        fun `Should have taxonomyunits @ root # taxonomyunit`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "taxonomyunit_0/taxonomyunit_info.json",
                expectedMarker = "fixed_taxonomyunit_0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "taxonomyunit_1/taxonomyunit_info.json",
                expectedMarker = "fixed_taxonomyunit_1"
            )
        }

        @Test
        fun `Should have codelists @ root # taxonomyunit # codelist`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "taxonomyunit_0/codelist_0/codelist.json",
                expectedMarker = "fixed_codelist_0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "taxonomyunit_0/codelist_1/codelist.json",
                expectedMarker = "fixed_codelist_1"
            )
        }

        @Test
        fun `Should have codepages @ root # taxonomyunit # codelist`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "taxonomyunit_0/codelist_0/codepage_0.json",
                expectedMarker = "fixed_codepage_0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "taxonomyunit_0/codelist_0/codepage_1.json",
                expectedMarker = "fixed_codepage_1"
            )
        }

        private fun assertTargetFolderHavingJsonFile(expectedFile: String, expectedMarker: String) {
            val expectedFilePath = targetFolderPath.resolve(expectedFile)
            assertThat(Files.isRegularFile(expectedFilePath)).isTrue()

            val json = objectMapper.readTree(expectedFilePath.toFile())
            assertThat(json.isObject).isTrue()
            assertThat(json.get("marker").textValue()).isEqualTo(expectedMarker)
        }
    }
}