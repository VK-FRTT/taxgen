package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files

@DisplayName("when RDS sources are recorded to folder")
internal class DpmSource_FolderRecorder_UnitTest : DpmSource_UnitTestBase() {

    @Nested
    @DisplayName("which is empty")
    inner class EmptyTargetFolder {

        private lateinit var tempFolder: TempFolder

        @BeforeAll
        fun init() {
            tempFolder = TempFolder("rds_recorder_blank")

            DpmSourceRecorderFolderAdapter(
                baseFolderPath = tempFolder.path(),
                forceOverwrite = false,
                diagnostic = diagnostic
            ).use {
                val (source, _) = dpmSourceFolderAdapterToReferenceData()
                it.captureSources(source)
            }
        }

        @AfterAll
        fun teardown() {
            tempFolder.close()
        }

        @Test
        fun `Should have source config at root`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "meta/source_config"
            )
        }

        @Test
        fun `Should have owner config`() {
            assertTargetFolderHavingJsonFile(
                expectedFile = "dpm_dictionary_0/dpm_owner_config"
            )

            assertTargetFolderHavingJsonFile(
                expectedFile = "dpm_dictionary_1/dpm_owner_config"
            )
        }

        @Nested
        inner class Concepts {

            @Test
            fun `Should have Metrics concept`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/met/code_list_meta"
                )
            }

            @Test
            fun `Should have ExplicitDomainsAndHierachies concept`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/code_list_meta"
                )
            }

            @Test
            fun `Should have TypedDomains concept`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/typ_dom/code_list_meta"
                )
            }

            @Test
            fun `Should have ExplicitDimensions concept`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dim/code_list_meta"
                )
            }

            @Test
            fun `Should have TypedDimensions concept`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/typ_dim/code_list_meta"
                )
            }
        }

        @Nested
        inner class ExplicitDomainsAndHierachiesConcept {

            @Test
            fun `Should have codepage`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/codes_page_0"
                )
            }

            @Test
            fun `Should have extension`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/extension_0/extension_meta"
                )
            }

            @Test
            fun `Should have extension member page`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_0"
                )
            }

            @Test
            fun `Should have sub code list`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/code_list_meta"
                )
            }

            @Test
            fun `Should have codepage within sub code list`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_0"
                )
            }

            @Test
            fun `Should have extension within sub code list`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/extension_meta"
                )
            }

            @Test
            fun `Should have extension members within sub code list`() {
                assertTargetFolderHavingJsonFile(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_0"
                )
            }
        }

        private fun assertTargetFolderHavingJsonFile(expectedFile: String) {
            val expectedFilePath = tempFolder.resolve("$expectedFile.json")
            assertThat(Files.isRegularFile(expectedFilePath)).isTrue()

            val json = objectMapper.readTree(expectedFilePath.toFile())
            assertThat(json.isObject).isTrue()
            assertThat(json.get("marker").textValue()).isEqualTo(expectedFile)
        }
    }
}
