package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps
import fi.vm.yti.taxgen.rdsprovider.rds.DpmSourceRdsAdapter
import fi.vm.yti.taxgen.testcommons.TempFolder
import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.SimulationSource
import io.specto.hoverfly.junit.dsl.HoverflyDsl.response
import io.specto.hoverfly.junit.dsl.HoverflyDsl.service
import io.specto.hoverfly.junit.dsl.ResponseCreators.success
import io.specto.hoverfly.junit.dsl.StubServiceBuilder
import io.specto.hoverfly.junit5.HoverflyExtension
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@DisplayName("Test RDS source adapter conformance")
@ExtendWith(HoverflyExtension::class)
internal class DpmSource_RdsAdapterConformance_UnitTest(private val hoverfly: Hoverfly) :
    DpmSource_ConformanceUnitTestBase() {

    internal enum class SimulationPhase {
        URL_RESOLUTION_URI_REDIRECT,
        URL_RESOLUTION_URI_METADATA,
        URL_RESOLUTION_EXPANDED_CODE_LIST,
        CONTENT_CODE_LIST_META,
        CONTENT_CODE_PAGE_0,
        CONTENT_CODE_PAGE_OTHER,
        CONTENT_EXTENSION_META,
        CONTENT_EXTENSION_MEMBER_0,
        CONTENT_EXTENSION_MEMBER_OTHER
    }

    internal enum class SimulationVariety {
        NONE,
        DELAY_RESPONSE,
    }

    internal data class DpmDictionarySimConf(
        val name: String,
        val codeLists: List<CodeListSimConf>
    )

    internal data class CodeListSimConf(
        val name: String,
        val codePages: List<CodePageSimConf> = emptyList(),
        val extensions: List<ExtensionSimConf> = emptyList()
    )

    internal data class CodePageSimConf(
        val name: String,
        val subCodeSchemeUri: String = ""
    )

    internal data class ExtensionSimConf(
        val name: String,
        val propertyTypeUri: String,
        val memberPages: List<MemberPageSimConf> = emptyList()
    )

    internal data class MemberPageSimConf(
        val name: String
    )

    internal companion object {
        lateinit var tempFolder: TempFolder
        lateinit var configFilePath: Path

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            tempFolder = TempFolder("conformance_rds_simulation")
            configFilePath = tempFolder.createFileWithContent("dpm_dictionary_config.json", rdsDpmSourceConfig())
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            tempFolder.close()
        }

        private fun rdsDpmSourceConfig(): String {
            val config = """
            {
              "marker": "meta/source_config",
              "dpmDictionaries": [
                {
                  "owner": {
                    "marker":"dpm_dictionary_0/dpm_owner_config",
                    "name": "Name_0",
                    "namespace": "Namespace_0",
                    "prefix": "Prefix_0",
                    "location": "Location_0",
                    "copyright": "Copyright_0",
                    "languages": [
                      "en",
                      "fi",
                      "sv"
                    ],
                    "defaultLanguage": "en"
                  },
                  "metrics": {
                    "uri": "http://uri.suomi.fi/codelist/dpm_dictionary_0/met"
                  },
                  "explicitDomainsAndHierarchies": {
                   "uri": "http://uri.suomi.fi/codelist/dpm_dictionary_0/exp_dom_hier"
                  },
                  "explicitDimensions": {
                    "uri": "http://uri.suomi.fi/codelist/dpm_dictionary_0/exp_dim"
                  },
                  "typedDomains": {
                    "uri": "http://uri.suomi.fi/codelist/dpm_dictionary_0/typ_dom"
                  },
                  "typedDimensions": {
                    "uri": "http://uri.suomi.fi/codelist/dpm_dictionary_0/typ_dim"
                  }
                },
                {
                  "owner": {
                    "marker":"dpm_dictionary_1/dpm_owner_config",
                    "name": "Name_1",
                    "namespace": "Namespace_1",
                    "prefix": "Prefix_1",
                    "location": "Location_1",
                    "copyright": "Copyright_1",
                    "languages": [
                      "en",
                      "fi",
                      "sv"
                    ],
                    "defaultLanguage": "en"
                  },
                  "metrics": {
                    "uri": null
                  },
                  "explicitDomainsAndHierarchies": {
                   "uri": "http://uri.suomi.fi/codelist/dpm_dictionary_1/exp_dom_hier"
                  },
                  "explicitDimensions": {
                    "uri": null
                  },
                  "typedDomains": {
                    "uri": null
                  },
                  "typedDimensions": {
                    "uri": null
                  }
                },

                ${blankDpmDictionaryConfig("dpm_dictionary_2/dpm_owner_config", "2")},
                ${blankDpmDictionaryConfig("dpm_dictionary_3/dpm_owner_config", "3")},
                ${blankDpmDictionaryConfig("dpm_dictionary_4/dpm_owner_config", "4")},
                ${blankDpmDictionaryConfig("dpm_dictionary_5/dpm_owner_config", "5")},
                ${blankDpmDictionaryConfig("dpm_dictionary_6/dpm_owner_config", "6")},
                ${blankDpmDictionaryConfig("dpm_dictionary_7/dpm_owner_config", "7")},
                ${blankDpmDictionaryConfig("dpm_dictionary_8/dpm_owner_config", "8")},
                ${blankDpmDictionaryConfig("dpm_dictionary_9/dpm_owner_config", "9")},
                ${blankDpmDictionaryConfig("dpm_dictionary_10/dpm_owner_config", "10")},
                ${blankDpmDictionaryConfig("dpm_dictionary_11/dpm_owner_config", "11")}
              ]
            }
            """.trimIndent()
            return config
        }

        private fun blankDpmDictionaryConfig(
            markerValue: String,
            tag: String
        ): String {
            return """{
                  "owner": {
                    "marker":"$markerValue",
                    "name": "Name_$tag",
                    "namespace": "Namespace_$tag",
                    "prefix": "Prefix_$tag",
                    "location": "Location_$tag",
                    "copyright": "Copyright_$tag",
                    "languages": [
                      "en",
                      "fi",
                      "sv"
                    ],
                    "defaultLanguage": "en"
                  },
                  "metrics": {
                    "uri": null
                  },
                  "explicitDomainsAndHierarchies": {
                   "uri": null
                  },
                  "explicitDimensions": {
                    "uri": null
                  },
                  "typedDomains": {
                    "uri": null
                  },
                  "typedDimensions": {
                    "uri": null
                  }
                }"""
        }
    }

    @TestFactory
    fun `RDS adapter with simulated HTTP responses`(): List<DynamicNode> {
        useCustomisedHttpClient()
        configureHoverflySimulation()

        val source = DpmSourceRdsAdapter(configFilePath, diagnostic)

        val expectedDetails = DpmSource_ConformanceUnitTestBase.ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "Reference Data service",
            dpmSourceContextIdentifier = configFilePath.toString()
        )

        return createAdapterConformanceTestCases(source, expectedDetails)
    }


    @ParameterizedTest(
        name = "Should handle timeouts within {0}"
    )
    @EnumSource(
        value = SimulationPhase::class,
        mode = EnumSource.Mode.MATCH_ANY,
        names = arrayOf(".*")
    )
    fun `Test request timeouts`(timeoutPhase: SimulationPhase) {
        useCustomisedHttpClient()

        configureHoverflySimulation(
            mapOf(
                timeoutPhase to SimulationVariety.DELAY_RESPONSE
            )
        )

        val source = DpmSourceRdsAdapter(configFilePath, diagnostic)

        var progress = "INIT"

        val thrown = catchThrowable {
            val dictionarySource = grabList<DpmDictionarySource> { source.eachDpmDictionarySource(it) }.first()

            val codeListSource = grabNullable<CodeListSource?> {
                dictionarySource.explicitDomainsAndHierarchiesSource(it)
            }!!

            codeListSource.codeListMetaData()
            progress = "CODE_LIST_META_DONE"

            codeListSource.codePagesData().toList()
            progress = "CODE_PAGES_DONE"

            val extensionSource = codeListSource.extensionSources().first()

            extensionSource.extensionMetaData()
            progress = "EXTENSION_META_DONE"

            extensionSource.extensionMemberPagesData().toList()
            progress = "EXTENSION_PAGES_DONE"
        }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] MESSAGE [The server communication timeout]"
        )

        when (timeoutPhase) {
            SimulationPhase.URL_RESOLUTION_URI_REDIRECT,
            SimulationPhase.URL_RESOLUTION_URI_METADATA,
            SimulationPhase.URL_RESOLUTION_EXPANDED_CODE_LIST,
            SimulationPhase.CONTENT_CODE_LIST_META -> assertThat(progress).isEqualTo("INIT")

            SimulationPhase.CONTENT_CODE_PAGE_0,
            SimulationPhase.CONTENT_CODE_PAGE_OTHER -> assertThat(progress).isEqualTo("CODE_LIST_META_DONE")

            SimulationPhase.CONTENT_EXTENSION_META -> assertThat(progress).isEqualTo("CODE_PAGES_DONE")

            SimulationPhase.CONTENT_EXTENSION_MEMBER_0,
            SimulationPhase.CONTENT_EXTENSION_MEMBER_OTHER -> assertThat(progress).isEqualTo("EXTENSION_META_DONE")
        }
    }

    private fun useCustomisedHttpClient() {
        val sslConfigurer = hoverfly.sslConfigurer

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslConfigurer.sslContext.socketFactory, sslConfigurer.trustManager)
            .readTimeout(50, TimeUnit.MILLISECONDS)
            .build()

        HttpOps.useCustomHttpClient(okHttpClient)
    }

    private fun configureHoverflySimulation(varietyConf: Map<SimulationPhase, SimulationVariety> = emptyMap()) {

        val simulationConf = listOf(
            DpmDictionarySimConf(
                name = "dpm_dictionary_0",
                codeLists = listOf(
                    CodeListSimConf(
                        name = "met"
                    ),
                    CodeListSimConf(
                        name = "exp_dom_hier",
                        codePages = listOf(
                            CodePageSimConf(
                                name = "codes_page_0",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_0"
                            ),
                            CodePageSimConf(
                                name = "codes_page_1",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_1"
                            ),
                            CodePageSimConf(
                                name = "codes_page_2",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_2"
                            ),
                            CodePageSimConf(
                                name = "codes_page_3",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_3"
                            ),
                            CodePageSimConf(
                                name = "codes_page_4",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_4"
                            ),
                            CodePageSimConf(
                                name = "codes_page_5",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_5"
                            ),
                            CodePageSimConf(
                                name = "codes_page_6",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_6"
                            ),
                            CodePageSimConf(
                                name = "codes_page_7",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_7"
                            ),
                            CodePageSimConf(
                                name = "codes_page_8",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_8"
                            ),
                            CodePageSimConf(
                                name = "codes_page_9",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_9"
                            ),
                            CodePageSimConf(
                                name = "codes_page_10",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_10"
                            ),
                            CodePageSimConf(
                                name = "codes_page_11",
                                subCodeSchemeUri = "http://uri.suomi.fi/codelist/dpm_dictionary_0/edh_sub_code_list_11"
                            )
                        ),
                        extensions = listOf(
                            ExtensionSimConf(
                                name = "extension_0",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_1"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_2"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_3"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_4"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_5"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_6"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_7"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_8"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_9"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_10"
                                    ),
                                    MemberPageSimConf(
                                        name = "members_page_11"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_1",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_2",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_3",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_4",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_5",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_6",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_7",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_8",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_9",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_10",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf(
                                name = "extension_11",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            ),
                            ExtensionSimConf( //Extension having unrecognized type => should get ignored in RDS source adapter
                                name = "extension_12",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#externalType",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            )
                        )
                    ),
                    CodeListSimConf(
                        name = "exp_dim"
                    ),
                    CodeListSimConf(
                        name = "typ_dom"
                    ),
                    CodeListSimConf(
                        name = "typ_dim"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_0",
                        codePages = listOf(
                            CodePageSimConf(
                                name = "codes_page_0"
                            )
                        ),
                        extensions = listOf(
                            ExtensionSimConf(
                                name = "extension_0",
                                propertyTypeUri = "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy",
                                memberPages = listOf(
                                    MemberPageSimConf(
                                        name = "members_page_0"
                                    )
                                )
                            )
                        )
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_1"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_2"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_3"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_4"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_5"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_6"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_7"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_8"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_9"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_10"
                    ),
                    CodeListSimConf(
                        name = "edh_sub_code_list_11"
                    )
                )
            ),

            DpmDictionarySimConf(
                name = "dpm_dictionary_1",
                codeLists = listOf(
                    CodeListSimConf(
                        name = "exp_dom_hier",
                        codePages = listOf(
                            CodePageSimConf(
                                name = "codes_page_0"
                            )
                        )
                    )
                )
            )
        )

        val uriService = service("uri.suomi.fi")
        val rdsService = service("koodistot.suomi.fi")

        simulationConf.forEach { dictionary ->

            dictionary.codeLists.forEach { codeList ->

                configureUrlResolutionResponses(
                    dictionary,
                    codeList,
                    uriService,
                    rdsService,
                    varietyConf
                )

                configureCodeListMetaAndCodePageResponses(
                    dictionary,
                    codeList,
                    rdsService,
                    varietyConf
                )

                configureCodeListExtensionMetaAndMemberPageResponses(
                    dictionary,
                    codeList,
                    rdsService,
                    varietyConf
                )
            }
        }

        hoverfly.simulate(SimulationSource.dsl(uriService, rdsService))
    }

    private fun configureUrlResolutionResponses(
        dictionary: DpmDictionarySimConf,
        codeList: CodeListSimConf,
        uriService: StubServiceBuilder,
        rdsService: StubServiceBuilder,
        varietyConf: Map<SimulationPhase, SimulationVariety>
    ) {
        //Content URL resolution: Redirect URI MetaData GET to RDS service
        uriService.redirectGet(
            currentPhase = SimulationPhase.URL_RESOLUTION_URI_REDIRECT,
            varietyConf = varietyConf,
            requestPath = "/codelist/${dictionary.name}/${codeList.name}",
            toTarget = "http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}"
        )

        //Content URL resolution: Response to URI MetaData GET
        rdsService.respondGetWithJson(
            currentPhase = SimulationPhase.URL_RESOLUTION_URI_METADATA,
            varietyConf = varietyConf,
            requestPath = "/taxgenfixture/${dictionary.name}/${codeList.name}",
            responseJson =
            """
            {
                "url":"http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}"
            }
            """.trimIndent()
        )

        //Content URL resolution: Response to expanded code list
        rdsService.respondGetWithJson(
            currentPhase = SimulationPhase.URL_RESOLUTION_EXPANDED_CODE_LIST,
            varietyConf = varietyConf,
            requestPath = "/taxgenfixture/${dictionary.name}/${codeList.name}",
            queryParams = listOf(Pair("expand", "extension")),
            responseJson =
            """
            {
              "url": "http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}",
              "codesUrl": "http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}/codes/",
              "extensions": [${codeList.extensions.map { extension ->
                """{
                        "url": "http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}/${extension.name}",
                        "membersUrl": "http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}/${extension.name}/members/",
                        "propertyType":{
                            "uri": "${extension.propertyTypeUri}"
                            }
                        }"""
                    .trimIndent()
            }.joinToString()}]
            }
            """.trimIndent()
        )
    }

    private fun configureCodeListMetaAndCodePageResponses(
        dictionary: DpmDictionarySimConf,
        codeList: CodeListSimConf,
        rdsService: StubServiceBuilder,
        varietyConf: Map<SimulationPhase, SimulationVariety>
    ) {
        //Content fetch: Code list meta
        rdsService.respondGetWithJson(
            currentPhase = SimulationPhase.CONTENT_CODE_LIST_META,
            varietyConf = varietyConf,
            requestPath = "/taxgenfixture/${dictionary.name}/${codeList.name}",
            queryParams = listOf(Pair("expand", "code")),
            responseJson = """
                        {
                            "marker": "${dictionary.name}/${codeList.name}/code_list_meta"
                        }
                        """.trimIndent()
        )

        codeList.codePages.forEachIndexed { index, codePage ->

            val nextPageLink = quoteIfNotNull(
                composePageIterationNextPageLinkOrNull(
                    index = index,
                    totalPages = codeList.codePages.size,
                    pageLinkBase = "http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}/codes/"
                )
            )

            val queryParams = composePageIterationQueryParams(index)

            //Content fetch: Codes page
            rdsService.respondGetWithJson(
                currentPhase = if (index == 0) {
                    SimulationPhase.CONTENT_CODE_PAGE_0
                } else {
                    SimulationPhase.CONTENT_CODE_PAGE_OTHER
                },
                varietyConf = varietyConf,
                requestPath = "/taxgenfixture/${dictionary.name}/${codeList.name}/codes/",
                queryParams = queryParams,
                responseJson = """
                        {
                          "meta": {
                            "nextPage": $nextPageLink
                          },

                          "marker": "${dictionary.name}/${codeList.name}/${codePage.name}/codes",

                          "results": [
                            {
                              "subCodeScheme": {
                                "uri": ${quoteIfNotNull(codePage.subCodeSchemeUri)}
                              }
                            }
                          ]
                        }
                        """.trimIndent()
            )
        }
    }

    private fun configureCodeListExtensionMetaAndMemberPageResponses(
        dictionary: DpmDictionarySimConf,
        codeList: CodeListSimConf,
        rdsService: StubServiceBuilder,
        varietyConf: Map<SimulationPhase, SimulationVariety>
    ) {
        codeList.extensions.forEach { extension ->

            //Content fetch: Extensions meta
            rdsService.respondGetWithJson(
                currentPhase = SimulationPhase.CONTENT_EXTENSION_META,
                varietyConf = varietyConf,
                requestPath = "/taxgenfixture/${dictionary.name}/${codeList.name}/${extension.name}",
                responseJson = """
                        {
                            "marker": "${dictionary.name}/${codeList.name}/${extension.name}/extension_meta"
                        }
                        """.trimIndent()
            )

            extension.memberPages.forEachIndexed { index, memberPage ->
                val nextPageLink = quoteIfNotNull(
                    composePageIterationNextPageLinkOrNull(
                        index = index,
                        totalPages = extension.memberPages.size,
                        pageLinkBase = "http://koodistot.suomi.fi/taxgenfixture/${dictionary.name}/${codeList.name}/${extension.name}/members/?expand=memberValue"
                    )
                )

                val queryParams =
                    composePageIterationQueryParams(index, Pair("expand", "memberValue"))

                //Content fetch: Extensions members page
                rdsService.respondGetWithJson(
                    currentPhase = if (index == 0) {
                        SimulationPhase.CONTENT_EXTENSION_MEMBER_0
                    } else {
                        SimulationPhase.CONTENT_EXTENSION_MEMBER_OTHER
                    },
                    varietyConf = varietyConf,
                    requestPath = "/taxgenfixture/${dictionary.name}/${codeList.name}/${extension.name}/members/",
                    queryParams = queryParams,
                    responseJson = """
                        {
                            "marker": "${dictionary.name}/${codeList.name}/${extension.name}/${memberPage.name}/members",

                            "meta": {
                              "nextPage": $nextPageLink
                            }
                        }
                        """.trimIndent()
                )
            }
        }
    }

    private fun composePageIterationNextPageLinkOrNull(
        index: Int,
        totalPages: Int,
        pageLinkBase: String
    ): String? {
        if (index == (totalPages - 1)) {
            return null
        }
        val fromValue = ((index + 1) * 1000).toString()

        return if (pageLinkBase.contains('?')) {
            "$pageLinkBase&pageSize=1000&from=$fromValue"
        } else {
            "$pageLinkBase?pageSize=1000&from=$fromValue"
        }
    }

    private fun quoteIfNotNull(value: String?): String? {
        if (value != null) {
            return "\"$value\""
        }

        return null
    }

    private fun composePageIterationQueryParams(
        index: Int,
        vararg extraParams: Pair<String, String>
    ): List<Pair<String, String>> {
        val params = mutableListOf(Pair("pageSize", "1000"))

        if (index > 0) {
            val fromValue = (index * 1000).toString()
            params.add(Pair("from", fromValue))
        }

        params.addAll(extraParams)
        return params
    }

    private fun StubServiceBuilder.redirectGet(
        currentPhase: SimulationPhase,
        varietyConf: Map<SimulationPhase, SimulationVariety>,
        requestPath: String,
        toTarget: String
    ): StubServiceBuilder {
        val requestMatcherBuilder = get(requestPath)

        val response = response()
            .status(303)
            .header("Location", toTarget)

        if (varietyConf[currentPhase] == SimulationVariety.DELAY_RESPONSE) {
            response.withDelay(100, TimeUnit.MILLISECONDS)
        }

        requestMatcherBuilder.willReturn(response)

        return this
    }

    private fun StubServiceBuilder.respondGetWithJson(
        currentPhase: SimulationPhase,
        varietyConf: Map<SimulationPhase, SimulationVariety>,
        requestPath: String,
        queryParams: List<Pair<String, String>>? = null,
        responseJson: String
    ): StubServiceBuilder {
        val requestMatcherBuilder = get(requestPath)

        queryParams?.forEach { it ->
            requestMatcherBuilder.queryParam(it.first, it.second)
        }

        val response = success(responseJson, "application/responseJson")

        if (varietyConf[currentPhase] == SimulationVariety.DELAY_RESPONSE) {
            response.withDelay(100, TimeUnit.MILLISECONDS)
        }

        requestMatcherBuilder.willReturn(response)

        return this
    }
}
