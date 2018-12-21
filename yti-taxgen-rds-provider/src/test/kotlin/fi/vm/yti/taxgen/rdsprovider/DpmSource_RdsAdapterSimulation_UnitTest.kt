package fi.vm.yti.taxgen.rdsprovider

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.config.OwnerConfig
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@DisplayName("when sources are read from simulated RDS API")
@ExtendWith(HoverflyExtension::class)
internal class DpmSource_RdsAdapterSimulation_UnitTest(private val hoverfly: Hoverfly) : DpmSource_UnitTestBase() {

    private lateinit var tempFolder: TempFolder
    private lateinit var configFilePath: Path
    private lateinit var dpmSource: DpmSource

    @BeforeEach
    fun testSuiteInit() {
        tempFolder = TempFolder("rds_adapter_simulation")

        useCustomisedHttpClient()

        configFilePath = tempFolder.createFileWithContent("dpm_dictionary_config.json", dpmDictionaryConfigContent())
        dpmSource = DpmSourceRdsAdapter(configFilePath, diagnostic)
    }

    @AfterEach
    fun testSuiteTeardown() {
        dpmSource.close()
        tempFolder.close()
    }

    @Nested
    @DisplayName("providing successful responses")
    inner class SuccessResponses {

        @BeforeEach
        fun init() {
            configureHoverflySimulation()
        }

        @Test
        fun `Should have source config at root`() {
            val configJson = objectMapper.readTree(dpmSource.sourceConfigData())
            assertThat(configJson.isObject).isTrue()
            assertThat(configJson.at("/dpmDictionaries/0/owner/name").asText()).isEqualTo("NameValue")
        }

        @Test
        fun `Should have diagnostic context info about RDS source`() {
            assertThat(dpmSource.contextType()).isEqualTo(DiagnosticContextType.DpmSource)
            assertThat(dpmSource.contextLabel()).isEqualTo("Reference Data service")
            assertThat(dpmSource.contextIdentifier()).isEqualTo(configFilePath.toString())
        }

        @Test
        fun `Should have owner config`() {
            val dpmDictionarySources = dpmSource.dpmDictionarySources().toList()
            assertThat(dpmDictionarySources.size).isEqualTo(1)

            val ownerConfig = objectMapper.readValue<OwnerConfig>(
                dpmDictionarySources[0].dpmOwnerConfigData()
            )

            assertThat(ownerConfig.name).isEqualTo("NameValue")
            assertThat(ownerConfig.namespace).isEqualTo("NamespaceValue")
            assertThat(ownerConfig.prefix).isEqualTo("PrefixValue")
            assertThat(ownerConfig.location).isEqualTo("LocationValue")
            assertThat(ownerConfig.copyright).isEqualTo("CopyrightValue")
            assertThat(ownerConfig.languages[0]).isEqualTo("en")
            assertThat(ownerConfig.languages[1]).isEqualTo("fi")
            assertThat(ownerConfig.defaultLanguage).isEqualTo("en")
        }

        @Test
        fun `Should have diagnostic context info about DPM dictionary`() {
            val dpmDictionarySources = dpmSource.dpmDictionarySources().toList()
            assertThat(dpmDictionarySources.size).isEqualTo(1)

            assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
            assertThat(dpmDictionarySources[0].contextLabel()).isEqualTo("")
            assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
        }

        @Nested
        inner class ExplicitDomainsAndHierarchiesConcept {

            @Test
            fun `Should have codelist`() {
                val codeList = dpmSource
                    .dpmDictionarySources()
                    .first()
                    .explicitDomainsAndHierarchiesSource()!!
                val marker = extractMarkerValueFromJsonData { codeList.codeListMetaData() }

                assertThat(marker).isEqualTo("simulated_codelist_0")
            }

            @Test
            fun `Should have diagnostic context`() {
                val codeList = dpmSource
                    .dpmDictionarySources()
                    .first()
                    .explicitDomainsAndHierarchiesSource()!!

                assertThat(codeList.contextType()).isEqualTo(DiagnosticContextType.RdsCodelist)
                assertThat(codeList.contextLabel()).isEqualTo("")
                assertThat(codeList.contextIdentifier()).isEqualTo("")
            }

            @Test
            fun `Should have codepages`() {
                val codesPages = dpmSource
                    .dpmDictionarySources()
                    .first()
                    .explicitDomainsAndHierarchiesSource()!!
                    .codePagesData()

                val markers = extractMarkerValuesFromJsonData(codesPages)
                { it -> (it as String) }

                assertThat(markers).containsExactly(
                    "simulated_codes_page_0",
                    "simulated_codes_page_1"
                )

                assertThat(diagnosticCollector.events).containsExactly(
                    "ENTER [InitConfiguration]",
                    "EXIT [InitConfiguration]",
                    "ENTER [InitUriResolution]",
                    "EXIT [InitUriResolution]",
                    "ENTER [CodesPage]",
                    "EXIT [CodesPage]",
                    "ENTER [CodesPage]",
                    "EXIT [CodesPage]"
                )
            }

            @Test
            fun `Should have extensions`() {
                val extensions = dpmSource
                    .dpmDictionarySources()
                    .first()
                    .explicitDomainsAndHierarchiesSource()!!
                    .extensionSources()

                val markers = extractMarkerValuesFromJsonData(extensions)
                { it -> (it as CodeListExtensionSource).extensionMetaData() }

                assertThat(markers).containsExactly(
                    "simulated_extension_0"
                )
            }

            @Test
            fun `Should have diagnostic context info about extension`() {
                val extensions = dpmSource
                    .dpmDictionarySources()
                    .first()
                    .explicitDomainsAndHierarchiesSource()!!
                    .extensionSources()
                    .toList()

                assertThat(extensions.size).isEqualTo(1)

                assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelistExtension)
                assertThat(extensions[0].contextLabel()).isEqualTo("")
                assertThat(extensions[0].contextIdentifier()).isEqualTo("")
            }

            @Test
            fun `Should have extension member pages`() {
                val extensionMemberPagesData = dpmSource
                    .dpmDictionarySources()
                    .first()
                    .explicitDomainsAndHierarchiesSource()!!
                    .extensionSources()
                    .first()
                    .extensionMemberPagesData()

                val markers = extractMarkerValuesFromJsonData(extensionMemberPagesData)
                { it -> (it as String) }

                assertThat(markers).containsExactly(
                    "simulated_extension_members_page_0",
                    "simulated_extension_members_page_1"
                )

                assertThat(diagnosticCollector.events).containsExactly(
                    "ENTER [InitConfiguration]",
                    "EXIT [InitConfiguration]",
                    "ENTER [InitUriResolution]",
                    "EXIT [InitUriResolution]",
                    "ENTER [YclCodelistExtensionMembersPage]",
                    "EXIT [YclCodelistExtensionMembersPage]",
                    "ENTER [YclCodelistExtensionMembersPage]",
                    "EXIT [YclCodelistExtensionMembersPage]"
                )
            }
        }
    }

    @Nested
    @DisplayName("providing errors")
    inner class ErrorResponses {

        @ParameterizedTest(
            name = "Should handle communication timeouts during {0} phase"
        )
        @EnumSource(
            value = SimulationPhase::class,
            mode = EnumSource.Mode.MATCH_ANY,
            names = arrayOf(".*")
        )
        fun testServerCommunicationTimeoutsAt(phase: SimulationPhase) {
            configureHoverflySimulation(
                mapOf(
                    phase to SimulationVariety.DELAY_RESPONSE
                )
            )

            val thrown = catchThrowable {
                val codeListSource = dpmSource
                    .dpmDictionarySources()
                    .first()
                    .explicitDomainsAndHierarchiesSource()!!

                codeListSource.codeListMetaData()
                codeListSource.codePagesData().toList()

                val extensionSource = codeListSource.extensionSources().first()
                extensionSource.extensionMetaData()
                extensionSource.extensionMemberPagesData().toList()

                val subCodeListSource = codeListSource.subCodeListSources().first()
                subCodeListSource.codeListMetaData()
                subCodeListSource.codePagesData().toList()
            }

            assertThat(thrown).isInstanceOf(HaltException::class.java)

            assertThat(diagnosticCollector.events).contains(
                "MESSAGE [FATAL] MESSAGE [The server communication timeout]"
            )
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

    private fun dpmDictionaryConfigContent(): String {
        return """
            {
              "dpmDictionaries": [
                {
                  "owner": {
                    "name": "NameValue",
                    "namespace": "NamespaceValue",
                    "prefix": "PrefixValue",
                    "location": "LocationValue",
                    "copyright": "CopyrightValue",
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
                }
              ]
            }
        """.trimIndent()
    }

    enum class SimulationPhase {
        URL_RESOLUTION_URI_REDIRECT,
        URL_RESOLUTION_CODESCHEME,
        URL_RESOLUTION_CODESCHEME_EXPANDED,
        CONTENT_CODESCHEME,
        CONTENT_CODE_PAGE_0,
        CONTENT_CODE_PAGE_1,
        CONTENT_EXTENSION,
        CONTENT_EXTENSION_MEMBER_0,
        CONTENT_EXTENSION_MEMBER_1
    }

    enum class SimulationVariety {
        NONE,
        DELAY_RESPONSE,
    }

    private fun configureHoverflySimulation(varietyConf: Map<SimulationPhase, SimulationVariety> = emptyMap()) {
        val simulationSource = SimulationSource.dsl(
            service("uri.suomi.fi")
                //URI redirects to RDS service
                .redirectGet(
                    currentPhase = SimulationPhase.URL_RESOLUTION_URI_REDIRECT,
                    varietyConf = varietyConf,
                    requestPath = "/codelist/ytitaxgenfixtures/minimal_zero",
                    toTarget = "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero"
                ),

            service("koodistot.suomi.fi")
                //RDS service responses for redirected URI
                .respondGetWithJson(
                    currentPhase = SimulationPhase.URL_RESOLUTION_CODESCHEME,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero",
                    responseJson = """
                        {
                            "url":"http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero",
                            "codesUrl":"http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/codes/"
                        }
                        """.trimIndent()
                )

                //RDS service responses for expanded CodeScheme (resolution phase)
                .respondGetWithJson(
                    currentPhase = SimulationPhase.URL_RESOLUTION_CODESCHEME_EXPANDED,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero",
                    queryParams = listOf(Pair("expand", "extension")),
                    responseJson = """
                        {
                          "url": "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero",
                          "codesUrl": "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/codes/",
                          "extensions": [
                            {
                              "url": "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/extensions/ext_0",
                              "membersUrl": "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/extensions/ext_0/members/"
                            }
                          ]
                        }
                        """.trimIndent()
                )

                //RDS service responses for expanded CodeScheme
                .respondGetWithJson(
                    currentPhase = SimulationPhase.CONTENT_CODESCHEME,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero",
                    queryParams = listOf(Pair("expand", "code")),
                    responseJson = """
                        {
                            "marker": "simulated_codelist_0"
                        }
                        """.trimIndent()
                )

                //RDS service responses for Code pages
                .respondGetWithJson(
                    currentPhase = SimulationPhase.CONTENT_CODE_PAGE_0,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero/codes/",
                    queryParams = listOf(Pair("pageSize", "1000")),
                    responseJson = """
                        {
                            "meta": {
                                "nextPage": "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/codes/?pageSize=1000&from=1000"
                            },
                            "marker": "simulated_codes_page_0"
                        }
                        """.trimIndent()
                )

                .respondGetWithJson(
                    currentPhase = SimulationPhase.CONTENT_CODE_PAGE_1,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero/codes/",
                    queryParams = listOf(Pair("pageSize", "1000"), Pair("from", "1000")),
                    responseJson = """
                        {
                            "meta": {
                                "nextPage": null
                            },
                            "marker": "simulated_codes_page_1"
                        }
                        """.trimIndent()
                )

                //RDS service responses for expanded Extension
                .respondGetWithJson(
                    currentPhase = SimulationPhase.CONTENT_EXTENSION,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero/extensions/ext_0",
                    responseJson = """
                        {
                            "marker": "simulated_extension_0"
                        }
                        """.trimIndent()
                )

                //RDS service responses for Extension Member pages
                .respondGetWithJson(
                    currentPhase = SimulationPhase.CONTENT_EXTENSION_MEMBER_0,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero/extensions/ext_0/members/",
                    queryParams = listOf(Pair("pageSize", "1000"), Pair("expand", "memberValue,code")),
                    responseJson = """
                        {
                            "meta": {
                                "nextPage": "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/extensions/ext_0/members/?pageSize=1000&from=1000"
                            },
                            "marker": "simulated_extension_members_page_0"
                        }
                        """.trimIndent()
                )

                .respondGetWithJson(
                    currentPhase = SimulationPhase.CONTENT_EXTENSION_MEMBER_1,
                    varietyConf = varietyConf,
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero/extensions/ext_0/members/",
                    queryParams = listOf(Pair("pageSize", "1000"), Pair("from", "1000")),
                    responseJson = """
                        {
                            "meta": {
                                "nextPage": null
                            },
                            "marker": "simulated_extension_members_page_1"
                        }
                        """.trimIndent()
                )
        )

        hoverfly.simulate(simulationSource)
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

