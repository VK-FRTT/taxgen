package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

open class DpmSource_ConformanceUnitTestBase : DpmSource_UnitTestBase() {

    data class ExpectedDetails(
        val dpmSourceContextType: DiagnosticContextType,
        val dpmSourceContextLabel: String,
        val dpmSourceContextIdentifier: String
    )

    protected fun createAdapterConformanceTestCases(
        dpmSource: DpmSource,
        expectedDetails: ExpectedDetails
    ): List<DynamicNode> {
        return listOf(
            dynamicContainer(
                "DpmSourceRoot",
                listOf(
                    dynamicTest("Should have diagnostic context info about RDS source") {
                        assertThat(dpmSource.contextType()).isEqualTo(expectedDetails.dpmSourceContextType)
                        assertThat(dpmSource.contextLabel()).isEqualTo(expectedDetails.dpmSourceContextLabel)
                        assertThat(dpmSource.contextIdentifier()).isEqualTo(expectedDetails.dpmSourceContextIdentifier)
                    },

                    dynamicTest("Should have source config") {
                        val marker = extractMarkerValueFromJsonData {
                            dpmSource.sourceConfigData()
                        }

                        assertThat(marker).isEqualTo("meta/source_config")
                    }
                )
            ),

            dynamicContainer(
                "DpmDictionaryUnit",
                listOf(

                    dynamicTest("Should have diagnostic context info about DPM dictionary") {
                        val dpmDictionarySources = dpmSource.dpmDictionarySources().toList()

                        //assertThat(dpmDictionarySources.size).isEqualTo(12)

                        assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
                        assertThat(dpmDictionarySources[0].contextLabel()).isEqualTo("")
                        assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
                        //assertThat(dpmDictionarySources[11].contextIdentifier()).isEqualTo("")
                    },

                    dynamicTest("Should have owner config") {
                        val dpmDictionarySources = dpmSource.dpmDictionarySources()

                        val markers =
                            extractMarkerValuesFromJsonData(dpmDictionarySources)
                            { it -> (it as DpmDictionarySource).dpmOwnerConfigData() }

                        assertThat(markers).containsExactly(
                            "dpm_dictionary_0/dpm_owner_config",
                            "dpm_dictionary_1/dpm_owner_config",
                            "dpm_dictionary_2/dpm_owner_config",
                            "dpm_dictionary_3/dpm_owner_config",
                            "dpm_dictionary_4/dpm_owner_config",
                            "dpm_dictionary_5/dpm_owner_config",
                            "dpm_dictionary_6/dpm_owner_config",
                            "dpm_dictionary_7/dpm_owner_config",
                            "dpm_dictionary_8/dpm_owner_config",
                            "dpm_dictionary_9/dpm_owner_config",
                            "dpm_dictionary_10/dpm_owner_config",
                            "dpm_dictionary_11/dpm_owner_config"
                        )
                    }
                )
            ),

            dynamicContainer(
                "MetricsConcept",
                listOf(
                    dynamicTest("Should provide source when concept folder exists in data") {
                        val metrics = dpmSource
                            .dpmDictionarySources().toList()[0]
                            .metricsSource()

                        assertThat(metrics).isNotNull()
                        assertMetricsBlueprint(metrics!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val metrics = dpmSource
                            .dpmDictionarySources().toList()[2]
                            .metricsSource()

                        assertThat(metrics).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val marker = extractMarkerValueFromJsonData {
                            dpmSource
                                .dpmDictionarySources().toList()[0]
                                .metricsSource()!!
                                .codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/met/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "ExplicitDomainsAndHierarchiesConcept",
                listOf(
                    dynamicTest("Should provide source when concept folder exists in data") {
                        val expDoms = dpmSource
                            .dpmDictionarySources().toList()[0]
                            .explicitDomainsAndHierarchiesSource()

                        assertThat(expDoms).isNotNull()
                        assertExplicitDomainsAndHierarchiesBlueprint(expDoms!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val expDoms = dpmSource
                            .dpmDictionarySources().toList()[2]
                            .explicitDomainsAndHierarchiesSource()

                        assertThat(expDoms).isNull()
                    },

                    dynamicContainer(
                        "ListOfDomains",
                        listOf(
                            dynamicTest("Should have codeList") {
                                val marker = extractMarkerValueFromJsonData {
                                    dpmSource
                                        .dpmDictionarySources().toList()[0]
                                        .explicitDomainsAndHierarchiesSource()!!
                                        .codeListMetaData()
                                }

                                assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dom_hier/code_list_meta")
                            },

                            dynamicTest("Should have diagnostic context info") {
                                val expDoms = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!

                                assertThat(expDoms.contextType()).isEqualTo(DiagnosticContextType.RdsCodelist)
                                assertThat(expDoms.contextLabel()).isEqualTo("")
                                assertThat(expDoms.contextIdentifier()).isEqualTo("")
                            },

                            dynamicTest("Should have codePages") {
                                val codepages = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .codePagesData()

                                val markers =
                                    extractMarkerValuesFromJsonData(codepages)
                                    { it -> it as String }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_0/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_1/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_2/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_3/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_4/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_5/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_6/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_7/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_8/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_9/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_10/codes",
                                    "dpm_dictionary_0/exp_dom_hier/codes_page_11/codes"
                                )
                            },

                            dynamicTest("Should have extensions") {
                                val extensions = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .extensionSources()
                                val markers =
                                    extractMarkerValuesFromJsonData(extensions)
                                    { it -> (it as CodeListExtensionSource).extensionMetaData() }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_1/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_2/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_3/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_4/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_5/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_6/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_7/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_8/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_9/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_10/extension_meta",
                                    "dpm_dictionary_0/exp_dom_hier/extension_11/extension_meta"
                                )
                            },

                            dynamicTest("Should have diagnostic context info about extension") {
                                val extensions = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .extensionSources().toList()

                                assertThat(extensions.size).isEqualTo(12)

                                assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelistExtension)
                                assertThat(extensions[0].contextLabel()).isEqualTo("")
                                assertThat(extensions[0].contextIdentifier()).isEqualTo("")
                                assertThat(extensions[11].contextIdentifier()).isEqualTo("")
                            },

                            dynamicTest("Should have extension members pages") {
                                val extensionPages = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .extensionSources().first()
                                    .extensionMemberPagesData()

                                val markers =
                                    extractMarkerValuesFromJsonData(extensionPages)
                                    { it -> it as String }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_0/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_1/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_2/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_3/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_4/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_5/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_6/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_7/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_8/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_9/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_10/members",
                                    "dpm_dictionary_0/exp_dom_hier/extension_0/members_page_11/members"
                                )
                            }
                        )
                    ),

                    dynamicContainer(
                        "DomainContentSubCodeLists",
                        listOf(

                            dynamicTest("Should provide subCodeListSources when available") {
                                val subCodeListSources = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .subCodeListSources().toList()

                                assertThat(subCodeListSources).isNotEmpty
                            },

                            dynamicTest("Should not provide subCodeListSources when not available") {
                                val subCodeListSources = dpmSource
                                    .dpmDictionarySources().toList()[1]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .subCodeListSources()
                                    .toList()

                                assertThat(subCodeListSources).isEmpty()
                            },

                            dynamicTest("Should have subCodeLists") {
                                val subCodeListSources = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .subCodeListSources()

                                val markers =
                                    extractMarkerValuesFromJsonData(subCodeListSources)
                                    { it -> (it as CodeListSource).codeListMetaData() }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_1/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_2/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_3/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_4/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_5/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_6/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_7/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_8/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_9/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_10/code_list_meta",
                                    "dpm_dictionary_0/edh_sub_code_list_11/code_list_meta"
                                )
                            },

                            dynamicTest("Should have diagnostic context info about subCodeLists") {
                                val subCodeListSources = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .subCodeListSources().toList()

                                assertThat(subCodeListSources.size).isEqualTo(12)

                                assertThat(subCodeListSources[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelist)
                                assertThat(subCodeListSources[0].contextLabel()).isEqualTo("")
                                assertThat(subCodeListSources[0].contextIdentifier()).isEqualTo("")
                                assertThat(subCodeListSources[11].contextIdentifier()).isEqualTo("")
                            },

                            dynamicTest("Should have codePages") {
                                val codepages = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .subCodeListSources().first()
                                    .codePagesData()

                                val markers =
                                    extractMarkerValuesFromJsonData(codepages)
                                    { it -> it as String }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/codes_page_0/codes"
                                )
                            },

                            dynamicTest("Should have extensions") {
                                val extensions = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .subCodeListSources().first()
                                    .extensionSources()
                                val markers =
                                    extractMarkerValuesFromJsonData(extensions)
                                    { it -> (it as CodeListExtensionSource).extensionMetaData() }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/extension_0/extension_meta"
                                )
                            },

                            dynamicTest("Should have diagnostic context info about extension") {
                                val extensions = dpmSource
                                    .dpmDictionarySources().toList()[0]
                                    .explicitDomainsAndHierarchiesSource()!!
                                    .subCodeListSources().first()
                                    .extensionSources().toList()

                                assertThat(extensions.size).isEqualTo(1)

                                assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelistExtension)
                                assertThat(extensions[0].contextLabel()).isEqualTo("")
                                assertThat(extensions[0].contextIdentifier()).isEqualTo("")
                            },

                            dynamicTest("Should have extension members pages") {
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
                                    "dpm_dictionary_0/edh_sub_code_list_0/extension_0/members_page_0/members"
                                )
                            }
                        )
                    )
                )
            ),

            dynamicContainer(
                "ExplicitDimensionsConcept",
                listOf(

                    dynamicTest("Should provide source when concept folder exists in data") {
                        val expDim = dpmSource
                            .dpmDictionarySources().toList()[0]
                            .explicitDimensionsSource()

                        assertThat(expDim).isNotNull()
                        assertExplictOrTypedDimensionsBlueprint(expDim!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val expDim = dpmSource
                            .dpmDictionarySources().toList()[2]
                            .explicitDimensionsSource()

                        assertThat(expDim).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val marker = extractMarkerValueFromJsonData {
                            dpmSource
                                .dpmDictionarySources().toList()[0]
                                .explicitDimensionsSource()!!
                                .codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dim/code_list_meta")
                    }
                )
            ),


            dynamicContainer(
                "TypedDomainsConcept",
                listOf(

                    dynamicTest("Should provide source when concept folder exists in data") {
                        val typDom = dpmSource
                            .dpmDictionarySources().toList()[0]
                            .typedDomainsSource()

                        assertThat(typDom).isNotNull()
                        assertTypedDomainBlueprint(typDom!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val typDom = dpmSource
                            .dpmDictionarySources().toList()[2]
                            .typedDomainsSource()

                        assertThat(typDom).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val marker = extractMarkerValueFromJsonData {
                            dpmSource
                                .dpmDictionarySources().toList()[0]
                                .typedDomainsSource()!!
                                .codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dom/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "TypedDimensionsConcept",
                listOf(
                    dynamicTest("Should provide source when concept folder exists in data") {
                        val typDim = dpmSource
                            .dpmDictionarySources().toList()[0]
                            .typedDimensionsSource()

                        assertThat(typDim).isNotNull()
                        assertExplictOrTypedDimensionsBlueprint(typDim!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val typDim = dpmSource
                            .dpmDictionarySources().toList()[2]
                            .typedDimensionsSource()

                        assertThat(typDim).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val marker = extractMarkerValueFromJsonData {
                            dpmSource
                                .dpmDictionarySources().toList()[0]
                                .typedDimensionsSource()!!
                                .codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dim/code_list_meta")
                    }
                )
            )
        )
    }

    private fun assertMetricsBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmMetric"
        )

        assertThat(blueprint.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint).isNull()
    }

    private fun assertExplicitDomainsAndHierarchiesBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain"
        )

        assertThat(blueprint.usesSubCodeLists).isTrue()
        assertThat(blueprint.subCodeListBlueprint).isNotNull()

        assertThat(blueprint.subCodeListBlueprint!!.usesExtensions).isTrue()
        assertThat(blueprint.subCodeListBlueprint!!.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy",
            "http://uri.suomi.fi/datamodel/ns/code#calculationHierarchy"
        )

        assertThat(blueprint.subCodeListBlueprint!!.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint!!.subCodeListBlueprint).isNull()
    }

    private fun assertTypedDomainBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmTypedDomain"
        )

        assertThat(blueprint.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint).isNull()
    }

    private fun assertExplictOrTypedDimensionsBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmDimension"
        )

        assertThat(blueprint.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint).isNull()
    }
}
