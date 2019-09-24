package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files

internal class DpmSource_FolderRecorder_ModuleTest : DpmSource_ModuleTestBase() {

    companion object {
        lateinit var emptyTargetFolder: TempFolder
        lateinit var emptyTargetEvents: List<String>

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            emptyTargetFolder = TempFolder("empty_target_folder")

            val diagnosticCollector = DiagnosticCollector()
            val diagnosticContext: DiagnosticContext = DiagnosticBridge(diagnosticCollector)

            SourceFactory.folderRecorder(
                outputFolderPath = emptyTargetFolder.path(),
                forceOverwrite = false,
                diagnosticContext = diagnosticContext
            ).use { sourceRecorder ->
                val (sourceHolder, _) = sourceHolderFolderAdapterForBundledReferenceData(
                    diagnosticContext,
                    true
                )

                sourceHolder.withDpmSource {
                    sourceRecorder.captureSources(it)
                }
            }

            emptyTargetEvents = diagnosticCollector.events

            println(emptyTargetEvents.joinToString(separator = "\n"))
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            emptyTargetFolder.close()
            emptyTargetEvents = emptyList()
        }
    }

    @Nested
    @DisplayName("which is empty")
    inner class EmptyTargetFolder {

        private fun assertTargetFolderHavingJsonFileWithMarker(
            expectedFile: String,
            expectedMarker: String
        ) {
            assertTargetFolderHavingJsonFileWithMarkerAt(
                expectedFile = expectedFile,
                expectedMarker = expectedMarker,
                markerLocation = "/marker"
            )
        }

        private fun assertTargetFolderHavingJsonFileWithMarkerAt(
            expectedFile: String,
            expectedMarker: String,
            markerLocation: String
        ) {
            val expectedFilePath = emptyTargetFolder.resolve("$expectedFile.json")
            assertThat(Files.isRegularFile(expectedFilePath)).isTrue()

            val json = objectMapper.readTree(expectedFilePath.toFile())
            assertThat(json.isObject).isTrue()
            val markerValue = json.nonBlankTextOrNullAt(markerLocation)

            assertThat(markerValue).isEqualTo(expectedMarker)
        }

        @Test
        fun `Should produce correct diagnostic events`() {
            assertThat(emptyTargetEvents).contains(
                "ENTER [DpmSourceRecorder] [folder]",
                "ENTER [DpmSource] [folder]",
                "ENTER [DpmDictionary] []",
                "ENTER [RdsCodeList] []"
            )
        }

        @Test
        fun `Should have source config at root`() {
            assertTargetFolderHavingJsonFileWithMarkerAt(
                expectedFile = "meta/source_config",
                expectedMarker = "dpm_dictionary_0/dpm_owner",
                markerLocation = "/dpmDictionaries/0/owner/name"
            )
        }

        @Test
        fun `Should have owner config`() {
            assertTargetFolderHavingJsonFileWithMarkerAt(
                expectedFile = "dpm_dictionary_0/dpm_owner_config",
                expectedMarker = "dpm_dictionary_0/dpm_owner",
                markerLocation = "/name"

            )

            assertTargetFolderHavingJsonFileWithMarkerAt(
                expectedFile = "dpm_dictionary_1/dpm_owner_config",
                expectedMarker = "dpm_dictionary_1/dpm_owner",
                markerLocation = "/name"
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
