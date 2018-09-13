package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureRecorder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files

@DisplayName("when ycl sources are recorded to folder structure")
internal class YclSource_FolderStructureRecorder_UnitTest : YclSource_UnitTestBase() {

    @Nested
    @DisplayName("which is empty")
    inner class EmptyTargetFolder {

        private lateinit var tempFolder: TempFolder

        @BeforeEach
        fun init() {
            tempFolder = TempFolder("yclsource_folder_structure_recorder_blank")

            YclSourceFolderStructureRecorder(
                baseFolderPath = tempFolder.path(),
                forceOverwrite = false,
                diagnostic = diagnostic
            ).use {
                it.captureSources(FixedYclSource())
            }
        }

        @AfterEach
        fun teardown() {
            tempFolder.close()
        }

        @Test
        fun `Should have source config at root`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "meta/source_config.json",
                expectedMarker = "fixed_source_config"
            )
        }

        @Test
        fun `Should have owner config @ root # dpmdictionary`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/dpm_owner_info.json",
                expectedMarker = "fixed_dpm_owner_config_0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_1/dpm_owner_info.json",
                expectedMarker = "fixed_dpm_owner_config_1"
            )
        }

        @Test
        fun `Should have codelist source config @ root # dpmdictionary # codelist`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codelist_source_config.json",
                expectedMarker = "fixed_codelist_source_config_0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_1/ycl_codelist_source_config.json",
                expectedMarker = "fixed_codelist_source_config_1"
            )
        }

        @Test
        fun `Should have codelists @ root # dpmdictionary # codelist`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codescheme.json",
                expectedMarker = "fixed_codescheme_0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_1/ycl_codescheme.json",
                expectedMarker = "fixed_codescheme_1"
            )
        }

        @Test
        fun `Should have codepages @ root # dpmdictionary # codelist`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codepage_0.json",
                expectedMarker = "fixed_codepage_0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codepage_1.json",
                expectedMarker = "fixed_codepage_1"
            )
        }

        private fun assertTargetFolderHavingJsonFile(expectedFile: String, expectedMarker: String) {
            val expectedFilePath = tempFolder.resolve(expectedFile)
            assertThat(Files.isRegularFile(expectedFilePath)).isTrue()

            val json = objectMapper.readTree(expectedFilePath.toFile())
            assertThat(json.isObject).isTrue()
            assertThat(json.get("marker").textValue()).isEqualTo(expectedMarker)
        }
    }
}
