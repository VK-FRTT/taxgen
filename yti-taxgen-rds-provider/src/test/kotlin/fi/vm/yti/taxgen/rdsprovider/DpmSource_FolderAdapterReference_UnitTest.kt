package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("when RDS sources are read from reference folder structure")
internal class DpmSource_FolderAdapterReference_UnitTest : DpmSource_UnitTestBase() {

    private lateinit var dpmSource: DpmSource
    private lateinit var resourcePath: Path

    @BeforeEach
    fun init() {
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceUri =
            classLoader.getResource("folder_adapter_reference").toURI()
        resourcePath = Paths.get(resourceUri)
        dpmSource = DpmSourceFolderAdapter(resourcePath)
    }

    @AfterEach
    fun teardown() {
        dpmSource.close()
    }

    @Nested
    inner class DpmSourceRoot {

        @Test
        fun `Should have diagnostic context info about RDS source`() {
            assertThat(dpmSource.contextType()).isEqualTo(DiagnosticContextType.DpmSource)
            assertThat(dpmSource.contextLabel()).isEqualTo("folder")
            assertThat(dpmSource.contextIdentifier()).isEqualTo(resourcePath.toString())
        }

        @Test
        fun `Should have source config`() {

            val marker = extractMarkerValueFromJsonData {
                dpmSource.sourceConfigData()
            }

            assertThat(marker).isEqualTo("folder_adapter_reference/meta/source_config")
        }
    }

    @Nested
    inner class DpmDictionaryUnit {

        @Test
        fun `Should have diagnostic context info about DPM dictionary`() {
            val dpmDictionarySources = dpmSource.dpmDictionarySources().toList()

            assertThat(dpmDictionarySources.size).isEqualTo(12)

            assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
            assertThat(dpmDictionarySources[0].contextLabel()).isEqualTo("")
            assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
            assertThat(dpmDictionarySources[11].contextIdentifier()).isEqualTo("")
        }

        @Test
        fun `Should have owner config`() {
            val dpmDictionarySources = dpmSource.dpmDictionarySources()

            val markers =
                extractMarkerValuesFromJsonData(dpmDictionarySources)
                { it -> (it as DpmDictionarySource).dpmOwnerConfigData() }

            assertThat(markers).containsExactly(
                "dpm_dictionary_0/owner_config",
                "dpm_dictionary_1/owner_config",
                "dpm_dictionary_2/owner_config",
                "dpm_dictionary_3/owner_config",
                "dpm_dictionary_4/owner_config",
                "dpm_dictionary_5/owner_config",
                "dpm_dictionary_6/owner_config",
                "dpm_dictionary_7/owner_config",
                "dpm_dictionary_8/owner_config",
                "dpm_dictionary_9/owner_config",
                "dpm_dictionary_10/owner_config",
                "dpm_dictionary_11/owner_config"
            )
        }
    }

    @Nested
    inner class MetricsConcept {

        @Test
        fun `Should provide source when concept folder exists in data`() {
            val metrics = dpmSource
                .dpmDictionarySources().toList()[0]
                .metricsSource()

            assertThat(metrics).isNotNull()
            assertMetricsBlueprint(metrics!!.blueprint())
        }

        @Test
        fun `Should not provide source when concept folder does not exist in data`() {
            val metrics = dpmSource
                .dpmDictionarySources().toList()[2]
                .metricsSource()

            assertThat(metrics).isNull()
        }

        @Test
        fun `Should have codeList`() {
            val marker = extractMarkerValueFromJsonData {
                dpmSource
                    .dpmDictionarySources().toList()[0]
                    .metricsSource()!!
                    .codeListMetaData()
            }

            assertThat(marker).isEqualTo("dpm_dictionary_0/met/code_list_meta")
        }
    }

    @Nested
    inner class ExplicitDomainsAndHierarchiesConcept {

        @Test
        fun `Should provide source when concept folder exists in data`() {
            val expDoms = dpmSource
                .dpmDictionarySources().toList()[0]
                .explicitDomainsAndHierarchiesSource()

            assertThat(expDoms).isNotNull()
            assertExplicitDomainsAndHierarchiesBlueprint(expDoms!!.blueprint())
        }

        @Test
        fun `Should not provide source when concept folder does not exist in data`() {
            val expDoms = dpmSource
                .dpmDictionarySources().toList()[2]
                .explicitDomainsAndHierarchiesSource()

            assertThat(expDoms).isNull()
        }

        @Nested
        inner class ListOfDomains {

            @Test
            fun `Should have codeList`() {
                val marker = extractMarkerValueFromJsonData {
                    dpmSource
                        .dpmDictionarySources().toList()[0]
                        .explicitDomainsAndHierarchiesSource()!!
                        .codeListMetaData()
                }

                assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dom_hier/code_list_meta")
            }

            @Test
            fun `Should have diagnostic context info`() {
                val expDoms = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!

                assertThat(expDoms.contextType()).isEqualTo(DiagnosticContextType.RdsCodelist)
                assertThat(expDoms.contextLabel()).isEqualTo("")
                assertThat(expDoms.contextIdentifier()).isEqualTo("")
            }

            @Test
            fun `Should have codePages`() {
                val codepages = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .codePagesData()

                val markers =
                    extractMarkerValuesFromJsonData(codepages)
                    { it -> it as String }

                assertThat(markers).containsExactly(
                    "dpm_dictionary_0/exp_dom_hier/codes_page_0"
                )
            }

            @Test
            fun `Should have extensions`() {
                val extensions = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .extensionSources()
                val markers =
                    extractMarkerValuesFromJsonData(extensions)
                    { it -> (it as CodeListExtensionSource).extensionMetaData() }

                assertThat(markers).containsExactly(
                    "dpm_dictionary_0/exp_dom_hier/extension_0/extension_meta"
                )
            }

            @Test
            fun `Should have diagnostic context info about extension`() {
                val extensions = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .extensionSources().toList()

                assertThat(extensions.size).isEqualTo(1)

                assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelistExtension)
                assertThat(extensions[0].contextLabel()).isEqualTo("")
                assertThat(extensions[0].contextIdentifier()).isEqualTo("")
            }

            @Test
            fun `Should have extension members pages`() {
                val extensionPages = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .extensionSources().first()
                    .extensionMemberPagesData()

                val markers =
                    extractMarkerValuesFromJsonData(extensionPages)
                    { it -> it as String }

                assertThat(markers).containsExactly(
                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_0"
                )
            }
        }

        @Nested
        inner class DomainContentSubCodeLists {

            @Test
            fun `Should provide subCodeListSources when available`() {
                val subCodeListSources = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources().toList()

                assertThat(subCodeListSources).isNotEmpty
            }

            @Test
            fun `Should not provide subCodeListSources when not available`() {
                val subCodeListSources = dpmSource
                    .dpmDictionarySources().toList()[1]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources()
                    .toList()

                assertThat(subCodeListSources).isEmpty()
            }

            @Test
            fun `Should have subCodeLists`() {
                val subCodeListSources = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources()

                val markers =
                    extractMarkerValuesFromJsonData(subCodeListSources)
                    { it -> (it as CodeListSource).codeListMetaData() }

                assertThat(markers).containsExactly(
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_1/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_2/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_3/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_4/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_5/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_6/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_7/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_8/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_9/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_10/code_list_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_11/code_list_meta"
                )
            }

            @Test
            fun `Should have diagnostic context info about subCodeLists`() {
                val subCodeListSources = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources().toList()

                assertThat(subCodeListSources.size).isEqualTo(12)

                assertThat(subCodeListSources[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelist)
                assertThat(subCodeListSources[0].contextLabel()).isEqualTo("")
                assertThat(subCodeListSources[0].contextIdentifier()).isEqualTo("")
                assertThat(subCodeListSources[11].contextIdentifier()).isEqualTo("")
            }

            @Test
            fun `Should have codePages`() {
                val codepages = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources().first()
                    .codePagesData()

                val markers =
                    extractMarkerValuesFromJsonData(codepages)
                    { it -> it as String }

                assertThat(markers).containsExactly(
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_0",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_1",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_2",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_3",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_4",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_5",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_6",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_7",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_8",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_9",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_10",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/codes_page_11"
                )
            }

            @Test
            fun `Should have extensions`() {
                val extensions = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources().first()
                    .extensionSources()
                val markers =
                    extractMarkerValuesFromJsonData(extensions)
                    { it -> (it as CodeListExtensionSource).extensionMetaData() }

                assertThat(markers).containsExactly(
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_1/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_2/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_3/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_4/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_5/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_6/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_7/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_8/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_9/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_10/extension_meta",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_11/extension_meta"
                )
            }

            @Test
            fun `Should have diagnostic context info about extension`() {
                val extensions = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources().first()
                    .extensionSources().toList()

                assertThat(extensions.size).isEqualTo(12)

                assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelistExtension)
                assertThat(extensions[0].contextLabel()).isEqualTo("")
                assertThat(extensions[0].contextIdentifier()).isEqualTo("")
                assertThat(extensions[11].contextIdentifier()).isEqualTo("")
            }

            @Test
            fun `Should have extension members pages`() {
                val extensionPages = dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDomainsAndHierarchiesSource()!!
                    .subCodeListSources().first()
                    .extensionSources().first()
                    .extensionMemberPagesData()

                val markers =
                    extractMarkerValuesFromJsonData(extensionPages)
                    { it -> it as String }

                assertThat(markers).containsExactly(
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_0",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_1",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_2",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_3",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_4",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_5",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_6",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_7",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_8",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_9",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_10",
                    "dpm_dictionary_0/exp_dom_hier/sub_code_list_0/extension_0/members_page_11"
                )
            }
        }
    }

    @Nested
    inner class ExplicitDimensionsConcept {

        @Test
        fun `Should provide source when concept folder exists in data`() {
            val expDim = dpmSource
                .dpmDictionarySources().toList()[0]
                .explicitDimensionsSource()

            assertThat(expDim).isNotNull()
            assertExplictOrTypedDimensionsBlueprint(expDim!!.blueprint())
        }

        @Test
        fun `Should not provide source when concept folder does not exist in data`() {
            val expDim = dpmSource
                .dpmDictionarySources().toList()[2]
                .explicitDimensionsSource()

            assertThat(expDim).isNull()
        }

        @Test
        fun `Should have codeList`() {
            val marker = extractMarkerValueFromJsonData {
                dpmSource
                    .dpmDictionarySources().toList()[0]
                    .explicitDimensionsSource()!!
                    .codeListMetaData()
            }

            assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dim/code_list_meta")
        }
    }

    @Nested
    inner class TypedDomainsConcept {

        @Test
        fun `Should provide source when concept folder exists in data`() {
            val typDom = dpmSource
                .dpmDictionarySources().toList()[0]
                .typedDomainsSource()

            assertThat(typDom).isNotNull()
            assertTypedDomainBlueprint(typDom!!.blueprint())
        }

        @Test
        fun `Should not provide source when concept folder does not exist in data`() {
            val typDom = dpmSource
                .dpmDictionarySources().toList()[2]
                .typedDomainsSource()

            assertThat(typDom).isNull()
        }

        @Test
        fun `Should have codeList`() {
            val marker = extractMarkerValueFromJsonData {
                dpmSource
                    .dpmDictionarySources().toList()[0]
                    .typedDomainsSource()!!
                    .codeListMetaData()
            }

            assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dom/code_list_meta")
        }
    }

    @Nested
    inner class TypedDimensionsConcept {

        @Test
        fun `Should provide source when concept folder exists in data`() {
            val typDim = dpmSource
                .dpmDictionarySources().toList()[0]
                .typedDimensionsSource()

            assertThat(typDim).isNotNull()
            assertExplictOrTypedDimensionsBlueprint(typDim!!.blueprint())
        }

        @Test
        fun `Should not provide source when concept folder does not exist in data`() {
            val typDim = dpmSource
                .dpmDictionarySources().toList()[2]
                .typedDimensionsSource()

            assertThat(typDim).isNull()
        }

        @Test
        fun `Should have codeList`() {
            val marker = extractMarkerValueFromJsonData {
                dpmSource
                    .dpmDictionarySources().toList()[0]
                    .typedDimensionsSource()!!
                    .codeListMetaData()
            }

            assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dim/code_list_meta")
        }
    }
}
