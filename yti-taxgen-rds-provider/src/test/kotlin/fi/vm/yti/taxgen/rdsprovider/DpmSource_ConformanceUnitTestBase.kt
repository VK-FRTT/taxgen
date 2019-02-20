package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

//TODO - Test with multiple code & extension member pages

open class DpmSource_ConformanceUnitTestBase : DpmSource_UnitTestBase() {

    data class ExpectedDetails(
        val dpmSourceContextType: DiagnosticContextType,
        val dpmSourceContextLabel: String,
        val dpmSourceContextIdentifier: String
    )

    protected fun <T> grabList(producer: ((T) -> Unit) -> Unit): List<T> {
        val list = mutableListOf<T>()

        producer {
            list.add(it)
        }

        return list
    }

    private fun <T : Any> grab(producer: ((T) -> Unit) -> Unit): T {
        lateinit var value: T

        producer {
            value = it
        }

        return value
    }

    protected fun <T : Any?> grabNullable(producer: ((T?) -> Unit) -> Unit): T? {
        var value: T? = null

        producer {
            value = it
        }

        return value
    }

    protected fun createAdapterConformanceTestCases(
        sourceProvider: SourceProvider,
        expectedDetails: ExpectedDetails
    ): List<DynamicNode> {

        val dpmSource = grab<DpmSource> {
            sourceProvider.withDpmSource(it)
        }

        return listOf(
            dynamicContainer(
                "DpmSourceRoot",
                listOf(
                    dynamicTest("Should have diagnostic context info about RDS source") {
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
                        val dpmDictionarySources = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }

                        assertThat(dpmDictionarySources.size).isEqualTo(12)

                        assertThat(dpmDictionarySources[0].contextLabel()).isEqualTo("")
                        assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
                        assertThat(dpmDictionarySources[11].contextIdentifier()).isEqualTo("")
                    },

                    dynamicTest("Should have owner config") {
                        val markers = mutableListOf<String>()

                        dpmSource.eachDpmDictionarySource { dictionarySource ->
                            dictionarySource.dpmOwnerConfigData {
                                extractMarkerValueFromJsonData(markers, it)
                            }
                        }

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
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val metrics = grabNullable<CodeListSource?> { dictionarySource.metricsSource(it) }

                        assertThat(metrics).isNotNull()
                        assertMetricsBlueprint(metrics!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val metrics = grabNullable<CodeListSource?> { dictionarySource.metricsSource(it) }

                        assertThat(metrics).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val metrics = grabNullable<CodeListSource?> { dictionarySource.metricsSource(it) }

                        val marker = extractMarkerValueFromJsonData {
                            metrics!!.codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/met/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "ExplicitDomainsAndHierarchiesConcept",
                listOf(
                    dynamicTest("Should provide source when concept folder exists in data") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val expDoms = grabNullable<CodeListSource?> {
                            dictionarySource.explicitDomainsAndHierarchiesSource(it)
                        }

                        assertThat(expDoms).isNotNull()
                        assertExplicitDomainsAndHierarchiesBlueprint(expDoms!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource =
                            grabList<DpmDictionarySource> { dpmSource.eachDpmDictionarySource(it) }[2]

                        val expDoms = grabNullable<CodeListSource?> {
                            dictionarySource.explicitDomainsAndHierarchiesSource(it)
                        }

                        assertThat(expDoms).isNull()
                    },

                    dynamicContainer(
                        "ListOfDomains",
                        listOf(
                            dynamicTest("Should have codeList") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }

                                val marker = extractMarkerValueFromJsonData {
                                    expDoms!!.codeListMetaData()
                                }

                                assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dom_hier/code_list_meta")
                            },

                            dynamicTest("Should have diagnostic context info") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                assertThat(expDoms.contextLabel()).isEqualTo("")
                                assertThat(expDoms.contextIdentifier()).contains("dpm_dictionary_0", "exp_dom_hier")
                            },

                            dynamicTest("Should have codePages") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val codePagesData = grabList<String> {
                                    expDoms.eachCodePageData(it)
                                }

                                val markers = extractMarkerValuesFromJsonData(codePagesData) { it }

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
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val extensionSources = grabList<ExtensionSource> {
                                    expDoms.eachExtensionSource(it)
                                }

                                val markers =
                                    extractMarkerValuesFromJsonData(extensionSources) { it.extensionMetaData() }

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
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val extensionSources = grabList<ExtensionSource> {
                                    expDoms.eachExtensionSource(it)
                                }

                                assertThat(extensionSources.size).isEqualTo(12)

                                assertThat(extensionSources[0].contextLabel()).isEqualTo("")
                                assertThat(extensionSources[0].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "exp_dom_hier",
                                    "extension_0"
                                )
                                assertThat(extensionSources[11].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "exp_dom_hier",
                                    "extension_11"
                                )
                            },

                            dynamicTest("Should have extension members pages") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val extensionSources = grabList<ExtensionSource> {
                                    expDoms.eachExtensionSource(it)
                                }

                                val extensionPages = grabList<String> {
                                    extensionSources.first().eachExtensionMemberPageData(it)
                                }

                                val markers = extractMarkerValuesFromJsonData(extensionPages) { it }

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
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                assertThat(subCodeListSources).isNotEmpty
                            },

                            dynamicTest("Should not provide subCodeListSources when not available") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[1]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                assertThat(subCodeListSources).isEmpty()
                            },

                            dynamicTest("Should have subCodeLists") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                val markers =
                                    extractMarkerValuesFromJsonData(subCodeListSources) { it.codeListMetaData() }

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
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                assertThat(subCodeListSources.size).isEqualTo(12)

                                assertThat(subCodeListSources[0].contextLabel()).isEqualTo("")
                                assertThat(subCodeListSources[0].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "sub_code_list_0"
                                )
                                assertThat(subCodeListSources[11].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "sub_code_list_11"
                                )
                            },

                            dynamicTest("Should have codePages") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                val codePagesData = grabList<String> {
                                    subCodeListSources.first().eachCodePageData(it)
                                }

                                val markers =
                                    extractMarkerValuesFromJsonData(codePagesData) { it }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/codes_page_0/codes"
                                )
                            },

                            dynamicTest("Should have extensions") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                val extensionSources = grabList<ExtensionSource> {
                                    subCodeListSources.first().eachExtensionSource(it)
                                }

                                val markers =
                                    extractMarkerValuesFromJsonData(extensionSources) { it.extensionMetaData() }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/extension_0/extension_meta"
                                )
                            },

                            dynamicTest("Should have diagnostic context info about extension") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                val extensionSources = grabList<ExtensionSource> {
                                    subCodeListSources.first().eachExtensionSource(it)
                                }

                                assertThat(extensionSources.size).isEqualTo(1)

                                assertThat(extensionSources[0].contextLabel()).isEqualTo("")
                                assertThat(extensionSources[0].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "sub_code_list_0",
                                    "extension_0"
                                )
                            },

                            dynamicTest("Should have extension members pages") {
                                val dictionarySource = grabList<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val expDoms = grabNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = grabList<CodeListSource> {
                                    expDoms.eachSubCodeListSource(it)
                                }

                                val extensionSources = grabList<ExtensionSource> {
                                    subCodeListSources.first().eachExtensionSource(it)
                                }

                                val extensionPages = grabList<String> {
                                    extensionSources.first().eachExtensionMemberPageData(it)
                                }

                                val markers = extractMarkerValuesFromJsonData(extensionPages) { it }

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
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.explicitDimensionsSource(it)
                        }

                        assertThat(codeListSource).isNotNull()
                        assertExplictOrTypedDimensionsBlueprint(codeListSource!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.explicitDimensionsSource(it)
                        }

                        assertThat(codeListSource).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.explicitDimensionsSource(it)
                        }!!

                        val marker = extractMarkerValueFromJsonData {
                            codeListSource.codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dim/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "TypedDomainsConcept",
                listOf(

                    dynamicTest("Should provide source when concept folder exists in data") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.typedDomainsSource(it)
                        }

                        assertThat(codeListSource).isNotNull()
                        assertTypedDomainBlueprint(codeListSource!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.typedDomainsSource(it)
                        }

                        assertThat(codeListSource).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.typedDomainsSource(it)
                        }!!

                        val marker = extractMarkerValueFromJsonData {
                            codeListSource.codeListMetaData()
                        }
                        assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dom/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "TypedDimensionsConcept",
                listOf(
                    dynamicTest("Should provide source when concept folder exists in data") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.typedDimensionsSource(it)
                        }

                        assertThat(codeListSource).isNotNull()
                        assertExplictOrTypedDimensionsBlueprint(codeListSource!!.blueprint())
                    },

                    dynamicTest("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.typedDimensionsSource(it)
                        }

                        assertThat(codeListSource).isNull()
                    },

                    dynamicTest("Should have codeList") {
                        val dictionarySource = grabList<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val codeListSource = grabNullable<CodeListSource?> {
                            dictionarySource.typedDimensionsSource(it)
                        }!!

                        val marker = extractMarkerValueFromJsonData {
                            codeListSource.codeListMetaData()
                        }
                        assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dim/code_list_meta")
                    }
                )
            )
        )
    }

    private fun assertMetricsBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmMetric",
            "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy"
        )

        assertThat(blueprint.subCodeListBlueprint).isNull()
    }

    private fun assertExplicitDomainsAndHierarchiesBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain"
        )

        assertThat(blueprint.subCodeListBlueprint).isNotNull()

        assertThat(blueprint.subCodeListBlueprint!!.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy",
            "http://uri.suomi.fi/datamodel/ns/code#calculationHierarchy"
        )

        assertThat(blueprint.subCodeListBlueprint!!.subCodeListBlueprint).isNull()
    }

    private fun assertTypedDomainBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmTypedDomain"
        )

        assertThat(blueprint.subCodeListBlueprint).isNull()
    }

    private fun assertExplictOrTypedDimensionsBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmDimension"
        )

        assertThat(blueprint.subCodeListBlueprint).isNull()
    }
}
