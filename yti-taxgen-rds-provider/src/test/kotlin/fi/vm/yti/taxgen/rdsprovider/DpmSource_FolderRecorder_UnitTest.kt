package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.testcommons.DiagnosticCollectorSimple
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files

@DisplayName("when RDS sources are recorded to folder")
internal class DpmSource_FolderRecorder_UnitTest : DpmSource_UnitTestBase() {

    companion object {
        lateinit var emptyTargetFolder: TempFolder

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            emptyTargetFolder = TempFolder("empty_target_folder")

            val dc = DiagnosticCollectorSimple()
            val d = DiagnosticBridge(dc)


            DpmSourceRecorderFolderAdapter(
                baseFolderPath = emptyTargetFolder.path(),
                forceOverwrite = false,
                diagnostic = d
            ).use {
                val (source, _) = dpmSourceFolderAdapterToReferenceData()
                it.captureSources(source)
            }
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            emptyTargetFolder.close()
        }
    }

    @Nested
    @DisplayName("which is empty")
    inner class EmptyTargetFolder {

        private fun assertTargetFolderHavingJsonFileWithMarker(
            expectedFile: String,
            expectedMarker: String
        ) {
            val expectedFilePath = emptyTargetFolder.resolve("$expectedFile.json")
            assertThat(Files.isRegularFile(expectedFilePath)).isTrue()

            val json = objectMapper.readTree(expectedFilePath.toFile())
            assertThat(json.isObject).isTrue()
            assertThat(json.get("marker").textValue()).isEqualTo(expectedMarker)
        }

        @Test
        fun `Should have source config at root`() {
            assertTargetFolderHavingJsonFileWithMarker(
                expectedFile = "meta/source_config",
                expectedMarker = "meta/source_config"
            )
        }

        @Test
        fun `Should have owner config`() {
            assertTargetFolderHavingJsonFileWithMarker(
                expectedFile = "dpm_dictionary_0/dpm_owner_config",
                expectedMarker = "dpm_dictionary_0/dpm_owner_config"
            )

            assertTargetFolderHavingJsonFileWithMarker(
                expectedFile = "dpm_dictionary_1/dpm_owner_config",
                expectedMarker = "dpm_dictionary_1/dpm_owner_config"
            )
        }

        @Nested
        inner class Concepts {

            @Test
            fun `Should have Metrics concept`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/met/code_list_meta",
                    expectedMarker = "dpm_dictionary_0/met/code_list_meta"
                )
            }

            @Test
            fun `Should have ExplicitDomainsAndHierarchies concept`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/code_list_meta",
                    expectedMarker = "dpm_dictionary_0/exp_dom_hier/code_list_meta"
                )
            }

            @Test
            fun `Should have TypedDomains concept`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/typ_dom/code_list_meta",
                    expectedMarker = "dpm_dictionary_0/typ_dom/code_list_meta"
                )
            }

            @Test
            fun `Should have ExplicitDimensions concept`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dim/code_list_meta",
                    expectedMarker = "dpm_dictionary_0/exp_dim/code_list_meta"
                )
            }

            @Test
            fun `Should have TypedDimensions concept`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/typ_dim/code_list_meta",
                    expectedMarker = "dpm_dictionary_0/typ_dim/code_list_meta"
                )
            }
        }

        @Nested
        inner class ExplicitDomainsAndHierarchiesConcept {

            @Test
            fun `Should have codepage`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/codes_page_0",
                    expectedMarker = "dpm_dictionary_0/exp_dom_hier/codes_page_0/codes"
                )
            }

            @Test
            fun `Should have extension`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/extension_0/extension_meta",
                    expectedMarker = "dpm_dictionary_0/exp_dom_hier/extension_0/extension_meta"
                )
            }

            @Test
            fun `Should have extension member page`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_0",
                    expectedMarker = "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_0/members"
                )
            }

            @Test
            fun `Should have sub code list`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/code_list_meta",
                    expectedMarker = "dpm_dictionary_0/edh_sub_code_list_0/code_list_meta"
                )
            }

            @Test
            fun `Should have codepage within sub code list`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_0",
                    expectedMarker = "dpm_dictionary_0/edh_sub_code_list_0/codes_page_0/codes"
                )
            }

            @Test
            fun `Should have extension within sub code list`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/extension_meta",
                    expectedMarker = "dpm_dictionary_0/edh_sub_code_list_0/extension_0/extension_meta"
                )
            }

            @Test
            fun `Should have extension members within sub code list`() {
                assertTargetFolderHavingJsonFileWithMarker(
                    expectedFile = "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_0",
                    expectedMarker = "dpm_dictionary_0/edh_sub_code_list_0/extension_0/members_page_0/members"
                )
            }
        }
    }
}
