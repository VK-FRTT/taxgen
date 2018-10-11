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
        fun `Should have owner config`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/dpm_owner_info.json",
                expectedMarker = "fixed_dpm_owner_config_d0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_1/dpm_owner_info.json",
                expectedMarker = "fixed_dpm_owner_config_d1"
            )
        }

        @Test
        fun `Should have codelist source config`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codelist_source_config.json",
                expectedMarker = "fixed_codelist_source_config_d0_c0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_1/ycl_codelist_source_config.json",
                expectedMarker = "fixed_codelist_source_config_d0_c1"
            )
        }

        @Test
        fun `Should have codelists`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codescheme.json",
                expectedMarker = "fixed_codescheme_d0_c0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_1/ycl_codescheme.json",
                expectedMarker = "fixed_codescheme_d0_c1"
            )
        }

        @Test
        fun `Should have codepages`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codes_page_0.json",
                expectedMarker = "fixed_codes_page_d0_c0_p0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/ycl_codes_page_1.json",
                expectedMarker = "fixed_codes_page_d0_c0_p1"
            )
        }

        @Test
        fun `Should have extensions`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/extension_0/ycl_extension.json",
                expectedMarker = "fixed_extension_d0_c0_e0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/extension_1/ycl_extension.json",
                expectedMarker = "fixed_extension_d0_c0_e1"
            )
        }

        @Test
        fun `Should have extension member pages`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/extension_0/ycl_extension_members_page_0.json",
                expectedMarker = "fixed_extension_member_d0_c0_e0_p0"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpmdictionary_0/codelist_0/extension_0/ycl_extension_members_page_1.json",
                expectedMarker = "fixed_extension_member_d0_c0_e0_p1"
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
