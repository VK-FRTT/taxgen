package fi.vm.yti.taxgen.rdsource

import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.ExceptionHarness.withHaltExceptionHarness
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest

open class DpmSource_FunctionalConformance_ModuleTestBase : DpmSource_ModuleTestBase() {

    data class ExpectedDetails(
        val dpmSourceContextType: DiagnosticContextType,
        val dpmSourceContextLabel: String,
        val dpmSourceContextIdentifier: String,
        val dpmSourceConfigFilePath: String
    )

    protected fun createAdapterConformanceTestCases(
        sourceHolder: SourceHolder,
        expectedDetails: ExpectedDetails
    ): List<DynamicNode> {

        val dpmSource = collect<DpmSource> {
            sourceHolder.withDpmSource(it)
        }

        return listOf(
            dynamicContainer(
                "DpmSource",
                listOf(
                    dynamicTestWithHaltExceptionHarness("Should have diagnostic context info about RDS source") {
                        assertThat(dpmSource.contextTitle()).isEqualTo(expectedDetails.dpmSourceContextLabel)
                        assertThat(dpmSource.contextIdentifier()).isEqualTo(expectedDetails.dpmSourceContextIdentifier)
                    },

                    dynamicTestWithHaltExceptionHarness("Should have source config") {
                        assertThat(
                            dpmSource.config().configFilePath
                        ).isEqualTo(expectedDetails.dpmSourceConfigFilePath)

                        assertThat(
                            collectMarkerValueFromJsonDataAt("/dpmDictionaries/0/owner/name") {
                                dpmSource.config().configData
                            }
                        ).isEqualTo("dpm_dictionary_0/dpm_owner")

                        assertThat(
                            dpmSource.config().dpmSourceConfig.dpmDictionaryConfigs.first().owner.name
                        ).isEqualTo("dpm_dictionary_0/dpm_owner")

                        assertThat(
                            dpmSource.config().processingOptions.sqliteDbMandatoryLabelLanguage!!.iso6391Code
                        ).isEqualTo("en")

                        assertThat(
                            dpmSource.config().processingOptions.sqliteDbMandatoryLabelSourceLanguages!![0].iso6391Code
                        ).isEqualTo("fi")

                        assertThat(
                            dpmSource.config().processingOptions.sqliteDbMandatoryLabelSourceLanguages!![1].iso6391Code
                        ).isEqualTo("sv")

                        assertThat(
                            dpmSource.config().processingOptions.sqliteDbDpmElementUriStorageLabelLanguage!!.iso6391Code
                        ).isEqualTo("pl")
                    }
                )
            ),

            dynamicContainer(
                "DpmDictionarySource",
                listOf(

                    dynamicTestWithHaltExceptionHarness("Should have diagnostic context info about DPM dictionary") {
                        val dpmDictionarySources = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }

                        assertThat(dpmDictionarySources.size).isEqualTo(12)

                        assertThat(dpmDictionarySources[0].contextTitle()).isEqualTo("")
                        assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
                        assertThat(dpmDictionarySources[11].contextIdentifier()).isEqualTo("")
                    },

                    dynamicTestWithHaltExceptionHarness("Should have owner objects") {
                        val markers = collectListOf<String> {
                            dpmSource.eachDpmDictionarySource { dictionarySource ->
                                dictionarySource.dpmOwner { owner ->
                                    it(owner.name)
                                }
                            }
                        }

                        assertThat(markers).containsExactly(
                            "dpm_dictionary_0/dpm_owner",
                            "dpm_dictionary_1/dpm_owner",
                            "dpm_dictionary_2/dpm_owner",
                            "dpm_dictionary_3/dpm_owner",
                            "dpm_dictionary_4/dpm_owner",
                            "dpm_dictionary_5/dpm_owner",
                            "dpm_dictionary_6/dpm_owner",
                            "dpm_dictionary_7/dpm_owner",
                            "dpm_dictionary_8/dpm_owner",
                            "dpm_dictionary_9/dpm_owner",
                            "dpm_dictionary_10/dpm_owner",
                            "dpm_dictionary_11/dpm_owner"
                        )
                    }
                )
            ),

            dynamicContainer(
                "Metrics CodeListSource",
                listOf(
                    dynamicTestWithHaltExceptionHarness("Should provide source when concept folder exists in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val metricsSource = collectNullable<CodeListSource?> { dictionarySource.metricsSource(it) }

                        assertThat(metricsSource).isNotNull()
                        assertMetricsBlueprint(metricsSource!!.blueprint())
                    },

                    dynamicTestWithHaltExceptionHarness("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val metricsSource = collectNullable<CodeListSource?> { dictionarySource.metricsSource(it) }

                        assertThat(metricsSource).isNull()
                    },

                    dynamicTestWithHaltExceptionHarness("Should have codeList") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val metricsSource = collectNullable<CodeListSource?> { dictionarySource.metricsSource(it) }

                        val marker = collectMarkerValueFromJsonData {
                            metricsSource!!.codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/met/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "ExplicitDomainsAndHierarchies CodeListSource",
                listOf(
                    dynamicTestWithHaltExceptionHarness("Should provide source when concept folder exists in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val explicitDomainsSource = collectNullable<CodeListSource?> {
                            dictionarySource.explicitDomainsAndHierarchiesSource(it)
                        }

                        assertThat(explicitDomainsSource).isNotNull()
                        assertExplicitDomainsAndHierarchiesBlueprint(explicitDomainsSource!!.blueprint())
                    },

                    dynamicTestWithHaltExceptionHarness("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val explicitDomainsSource = collectNullable<CodeListSource?> {
                            dictionarySource.explicitDomainsAndHierarchiesSource(it)
                        }

                        assertThat(explicitDomainsSource).isNull()
                    },

                    dynamicContainer(
                        "List of Domains",
                        listOf(
                            dynamicTestWithHaltExceptionHarness("Should have codeList") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }

                                val marker = collectMarkerValueFromJsonData {
                                    explicitDomainsSource!!.codeListMetaData()
                                }

                                assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dom_hier/code_list_meta")
                            },

                            dynamicTestWithHaltExceptionHarness("Should have diagnostic context info") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                assertThat(explicitDomainsSource.contextTitle()).isEqualTo("")
                                assertThat(explicitDomainsSource.contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "exp_dom_hier"
                                )
                            },

                            dynamicTestWithHaltExceptionHarness("Should have codePages") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val markers = collectMarkerValueFromEachJsonData {
                                    explicitDomainsSource.eachCodePageData(it)
                                }

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

                            dynamicTestWithHaltExceptionHarness("Should have extensions") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val extensionSources = collectListOf<ExtensionSource> {
                                    explicitDomainsSource.eachExtensionSource(it)
                                }

                                val markers = collectMarkerValueFromEachJsonData { markerExtractAction ->
                                    extensionSources.forEach { extensionSource ->
                                        markerExtractAction(extensionSource.extensionMetaData())
                                    }
                                }

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

                            dynamicTestWithHaltExceptionHarness("Should have diagnostic context info about extension") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val extensionSources = collectListOf<ExtensionSource> {
                                    explicitDomainsSource.eachExtensionSource(it)
                                }

                                assertThat(extensionSources.size).isEqualTo(12)

                                assertThat(extensionSources[0].contextTitle()).isEqualTo("")
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

                            dynamicTestWithHaltExceptionHarness("Should have extension members pages") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val extensionSources = collectListOf<ExtensionSource> {
                                    explicitDomainsSource.eachExtensionSource(it)
                                }

                                val markers = collectMarkerValueFromEachJsonData { markerExtractAction ->
                                    extensionSources.first().eachExtensionMemberPageData(markerExtractAction)
                                }

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
                        "Domain Content via SubCodeLists",
                        listOf(

                            dynamicTestWithHaltExceptionHarness("Should provide subCodeListSources when available") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                assertThat(subCodeListSources).isNotEmpty
                            },

                            dynamicTestWithHaltExceptionHarness("Should not provide subCodeListSources when not available") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[1]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                assertThat(subCodeListSources).isEmpty()
                            },

                            dynamicTestWithHaltExceptionHarness("Should have subCodeLists") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                val markers = collectMarkerValueFromEachJsonData { markerExtractAction ->
                                    subCodeListSources.forEach {
                                        markerExtractAction(it.codeListMetaData())
                                    }
                                }

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

                            dynamicTestWithHaltExceptionHarness("Should have diagnostic context info about subCodeLists") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                assertThat(subCodeListSources.size).isEqualTo(12)

                                assertThat(subCodeListSources[0].contextTitle()).isEqualTo("")
                                assertThat(subCodeListSources[0].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "sub_code_list_0"
                                )
                                assertThat(subCodeListSources[11].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "sub_code_list_11"
                                )
                            },

                            dynamicTestWithHaltExceptionHarness("Should have codePages") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                val markers = collectMarkerValueFromEachJsonData { markerExtractAction ->
                                    subCodeListSources.first().eachCodePageData(markerExtractAction)
                                }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/codes_page_0/codes"
                                )
                            },

                            dynamicTestWithHaltExceptionHarness("Should have extensions") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                val extensionSources = collectListOf<ExtensionSource> {
                                    subCodeListSources.first().eachExtensionSource(it)
                                }

                                val markers = collectMarkerValueFromEachJsonData { markerExtractAction ->
                                    extensionSources.forEach {
                                        markerExtractAction(it.extensionMetaData())
                                    }
                                }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/extension_0/extension_meta"
                                )
                            },

                            dynamicTestWithHaltExceptionHarness("Should have diagnostic context info about extension") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                val extensionSources = collectListOf<ExtensionSource> {
                                    subCodeListSources.first().eachExtensionSource(it)
                                }

                                assertThat(extensionSources.size).isEqualTo(1)

                                assertThat(extensionSources[0].contextTitle()).isEqualTo("")
                                assertThat(extensionSources[0].contextIdentifier()).contains(
                                    "dpm_dictionary_0",
                                    "sub_code_list_0",
                                    "extension_0"
                                )
                            },

                            dynamicTestWithHaltExceptionHarness("Should have extension members pages") {
                                val dictionarySource = collectListOf<DpmDictionarySource> {
                                    dpmSource.eachDpmDictionarySource(it)
                                }[0]

                                val explicitDomainsSource = collectNullable<CodeListSource?> {
                                    dictionarySource.explicitDomainsAndHierarchiesSource(it)
                                }!!

                                val subCodeListSources = collectListOf<CodeListSource> {
                                    explicitDomainsSource.eachSubCodeListSource(it)
                                }

                                val extensionSources = collectListOf<ExtensionSource> {
                                    subCodeListSources.first().eachExtensionSource(it)
                                }

                                val markers = collectMarkerValueFromEachJsonData { markerExtractAction ->
                                    extensionSources.first().eachExtensionMemberPageData(markerExtractAction)
                                }

                                assertThat(markers).containsExactly(
                                    "dpm_dictionary_0/edh_sub_code_list_0/extension_0/members_page_0/members"
                                )
                            }
                        )
                    )
                )
            ),

            dynamicContainer(
                "ExplicitDimensions CodeListSource",
                listOf(

                    dynamicTestWithHaltExceptionHarness("Should provide source when concept folder exists in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val explicitDimensionsSource = collectNullable<CodeListSource?> {
                            dictionarySource.explicitDimensionsSource(it)
                        }

                        assertThat(explicitDimensionsSource).isNotNull()
                        assertExplicitOrTypedDimensionsBlueprint(explicitDimensionsSource!!.blueprint())
                    },

                    dynamicTestWithHaltExceptionHarness("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val explicitDimensionsSource = collectNullable<CodeListSource?> {
                            dictionarySource.explicitDimensionsSource(it)
                        }

                        assertThat(explicitDimensionsSource).isNull()
                    },

                    dynamicTestWithHaltExceptionHarness("Should have codeList") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val explicitDimensionsSource = collectNullable<CodeListSource?> {
                            dictionarySource.explicitDimensionsSource(it)
                        }!!

                        val marker = collectMarkerValueFromJsonData {
                            explicitDimensionsSource.codeListMetaData()
                        }

                        assertThat(marker).isEqualTo("dpm_dictionary_0/exp_dim/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "TypedDomains CodeListSource",
                listOf(

                    dynamicTestWithHaltExceptionHarness("Should provide source when concept folder exists in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val typedDomainsSource = collectNullable<CodeListSource?> {
                            dictionarySource.typedDomainsSource(it)
                        }

                        assertThat(typedDomainsSource).isNotNull()
                        assertTypedDomainBlueprint(typedDomainsSource!!.blueprint())
                    },

                    dynamicTestWithHaltExceptionHarness("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val typedDomainsSource = collectNullable<CodeListSource?> {
                            dictionarySource.typedDomainsSource(it)
                        }

                        assertThat(typedDomainsSource).isNull()
                    },

                    dynamicTestWithHaltExceptionHarness("Should have codeList") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val typedDomainsSource = collectNullable<CodeListSource?> {
                            dictionarySource.typedDomainsSource(it)
                        }!!

                        val marker = collectMarkerValueFromJsonData {
                            typedDomainsSource.codeListMetaData()
                        }
                        assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dom/code_list_meta")
                    }
                )
            ),

            dynamicContainer(
                "TypedDimensions CodeListSource",
                listOf(
                    dynamicTestWithHaltExceptionHarness("Should provide source when concept folder exists in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val typedDimensionsSource = collectNullable<CodeListSource?> {
                            dictionarySource.typedDimensionsSource(it)
                        }

                        assertThat(typedDimensionsSource).isNotNull()
                        assertExplicitOrTypedDimensionsBlueprint(typedDimensionsSource!!.blueprint())
                    },

                    dynamicTestWithHaltExceptionHarness("Should not provide source when concept folder does not exist in data") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[2]

                        val typedDimensionsSource = collectNullable<CodeListSource?> {
                            dictionarySource.typedDimensionsSource(it)
                        }

                        assertThat(typedDimensionsSource).isNull()
                    },

                    dynamicTestWithHaltExceptionHarness("Should have codeList") {
                        val dictionarySource = collectListOf<DpmDictionarySource> {
                            dpmSource.eachDpmDictionarySource(it)
                        }[0]

                        val typedDimensionsSource = collectNullable<CodeListSource?> {
                            dictionarySource.typedDimensionsSource(it)
                        }!!

                        val marker = collectMarkerValueFromJsonData {
                            typedDimensionsSource.codeListMetaData()
                        }
                        assertThat(marker).isEqualTo("dpm_dictionary_0/typ_dim/code_list_meta")
                    }
                )
            )
        )
    }

    private fun dynamicTestWithHaltExceptionHarness(displayName: String, testSteps: () -> Unit): DynamicTest {
        return dynamicTest(displayName) {
            withHaltExceptionHarness(
                diagnosticCollector = diagnosticCollector,
                exceptionIsExpected = false
            ) {
                testSteps()
            }
        }
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

    private fun assertExplicitOrTypedDimensionsBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmDimension"
        )

        assertThat(blueprint.subCodeListBlueprint).isNull()
    }

    protected fun <T> collectListOf(action: ((T) -> Unit) -> Unit): List<T> {
        val list = mutableListOf<T>()

        action {
            list.add(it)
        }

        return list
    }

    private fun <T : Any> collect(action: ((T) -> Unit) -> Unit): T {
        lateinit var value: T

        action {
            value = it
        }

        return value
    }

    protected fun <T : Any?> collectNullable(action: ((T?) -> Unit) -> Unit): T? {
        var value: T? = null

        action {
            value = it
        }

        return value
    }

    private fun collectMarkerValueFromEachJsonData(eachAction: (markerExtractAction: (String) -> Unit) -> Unit): List<String?> {
        return collectMarkerValueFromEachJsonDataAt(
            "/marker",
            eachAction
        )
    }

    private fun collectMarkerValueFromEachJsonDataAt(
        markerLocation: String,
        eachAction: (markerExtractAction: (String) -> Unit) -> Unit
    ): List<String?> {
        val markers = mutableListOf<String?>()

        withHaltExceptionHarness(diagnosticCollector, false) {
            eachAction { jsonData ->
                val json = objectMapper.readTree(jsonData)
                assertThat(json.isObject).isTrue()
                markers.add(json.nonBlankTextOrNullAt(markerLocation))
            }
        }

        return markers
    }

    private fun collectMarkerValueFromJsonData(
        action: () -> String
    ): String? {
        return collectMarkerValueFromJsonDataAt(
            "/marker",
            action
        )
    }

    private fun collectMarkerValueFromJsonDataAt(
        markerLocation: String,
        action: () -> String
    ): String? {
        var marker: String? = null

        withHaltExceptionHarness(diagnosticCollector, false) {
            val jsonData = action()
            val json = objectMapper.readTree(jsonData)
            assertThat(json.isObject).isTrue()
            marker = json.nonBlankTextOrNullAt(markerLocation)
        }

        return marker
    }
}
