package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundleWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

internal class FolderSourceBundleWriter_UnitTest {

    @Nested
    @DisplayName("when writing bundle contents to blank folder")
    inner class WriteToFilesystem {

        private val objectMapper = jacksonObjectMapper()
        private lateinit var sourceBundle: SourceBundle
        private lateinit var targetFolderPath: Path
        private lateinit var bundleWriter: SourceBundleWriter

        @BeforeEach
        fun init() {
            sourceBundle = FixedSourceBundle()
            targetFolderPath = Files.createTempDirectory("foldersourcebundlewriter_unittest")
            bundleWriter = FolderSourceBundleWriter(targetFolderPath, sourceBundle, false)
        }

        @AfterEach
        fun teardown() {
            Files
                .walk(targetFolderPath)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }

        @Test
        fun `Should write bundleinfo to root`() {
            bundleWriter.write()

            assertTargetFolderHavingJsonFile(
                expetedFile = "bundle_info.json",
                expectedMarker = "fixed_source_bundle"
            )
        }

        @Test
        fun `Should write taxonomyUnits under root # taxonomyunit`() {
            bundleWriter.write()

            assertTargetFolderHavingJsonFile(
                expetedFile = "taxonomyunit_0/taxonomyunit_info.json",
                expectedMarker = "fixed_taxonomyunit_0"
            )

            assertTargetFolderHavingJsonFile(
                expetedFile = "taxonomyunit_1/taxonomyunit_info.json",
                expectedMarker = "fixed_taxonomyunit_1"
            )
        }

        @Test
        fun `Should write codeLists under root # taxonomyunit # codelist`() {
            bundleWriter.write()

            assertTargetFolderHavingJsonFile(
                expetedFile = "taxonomyunit_0/codelist_0/codelist.json",
                expectedMarker = "fixed_codelist_0"
            )

            assertTargetFolderHavingJsonFile(
                expetedFile = "taxonomyunit_0/codelist_1/codelist.json",
                expectedMarker = "fixed_codelist_1"
            )
        }

        @Test
        fun `Should write codePages under root # taxonomyunit # codelist`() {
            bundleWriter.write()

            assertTargetFolderHavingJsonFile(
                expetedFile = "taxonomyunit_0/codelist_0/codepage_0.json",
                expectedMarker = "fixed_codepage_0"
            )

            assertTargetFolderHavingJsonFile(
                expetedFile = "taxonomyunit_0/codelist_0/codepage_1.json",
                expectedMarker = "fixed_codepage_1"
            )
        }

        private fun assertTargetFolderHavingJsonFile(expetedFile: String, expectedMarker: String) {
            val expectedFilePath = targetFolderPath.resolve(expetedFile)
            assertThat(Files.isRegularFile(expectedFilePath)).isTrue()

            val json = objectMapper.readTree(expectedFilePath.toFile())
            assertThat(json.isObject).isTrue()
            assertThat(json.get("marker").textValue()).isEqualTo(expectedMarker)
        }
    }
}
