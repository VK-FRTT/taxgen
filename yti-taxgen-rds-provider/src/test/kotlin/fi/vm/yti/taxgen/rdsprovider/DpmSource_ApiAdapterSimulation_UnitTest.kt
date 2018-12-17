package fi.vm.yti.taxgen.rdsprovider

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.rdsprovider.rds.DpmSourceRdsAdapter
import fi.vm.yti.taxgen.rdsprovider.config.OwnerConfig
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps
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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@DisplayName("when YCL sources are read from simulated YCL API")
@ExtendWith(HoverflyExtension::class)
internal class DpmSource_ApiAdapterSimulation_UnitTest(private val hoverfly: Hoverfly) : DpmSource_UnitTestBase() {

    private lateinit var tempFolder: TempFolder
    private lateinit var configFilePath: Path
    private lateinit var dpmSource: DpmSource

    @BeforeEach
    fun testSuiteInit() {
        tempFolder = TempFolder("yclsource_apiadapter_unittest")

        useCustomisedHttpClient()

        configFilePath = tempFolder.createFileWithContent("ycl_source_config.json", yclSourceConfig())
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

            assertThat(configJson.at("/dpmDictionaries/0/owner/name").asText()).isEqualTo("OwnerName")
            assertThat(configJson.at("/dpmDictionaries/0/codelists/0/domainCode").asText()).isEqualTo("m_zero_override")
        }

        @Test
        fun `Should have diagnostic context info about yclsource`() {
            assertThat(dpmSource.contextType()).isEqualTo(DiagnosticContextType.DpmSource)
            assertThat(dpmSource.contextLabel()).isEqualTo("YTI Reference Data service")
            assertThat(dpmSource.contextIdentifier()).isEqualTo(configFilePath.toString())
        }

        @Test
        fun `Should have owner config`() {
            val dpmDictionarySources = dpmSource.dpmDictionarySources()
            assertThat(dpmDictionarySources.size).isEqualTo(1)

            val ownerConfig = objectMapper.readValue<OwnerConfig>(
                dpmSource.dpmDictionarySources()[0].dpmOwnerConfigData()
            )

            assertThat(ownerConfig.name).isEqualTo("OwnerName")
            assertThat(ownerConfig.namespace).isEqualTo("OwnerNamespace")
            assertThat(ownerConfig.prefix).isEqualTo("OwnerPrefix")
            assertThat(ownerConfig.location).isEqualTo("OwnerLocation")
            assertThat(ownerConfig.copyright).isEqualTo("OwnerCopyright")
            assertThat(ownerConfig.languages[0]).isEqualTo("en")
            assertThat(ownerConfig.languages[1]).isEqualTo("fi")
            assertThat(ownerConfig.defaultLanguage).isEqualTo("en")
        }

        @Test
        fun `Should have diagnostic context info about dpmdictionary`() {
            val dpmDictionarySources = dpmSource.dpmDictionarySources()
            assertThat(dpmDictionarySources.size).isEqualTo(1)

            assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
            assertThat(dpmDictionarySources[0].contextLabel()).isEqualTo("")
            assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
        }

        @Test
        fun `Should have codelists`() {
            val codeLists = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()
            val markers =
                extractMarkerValuesFromJsonData(
                    codeLists,
                    { it -> (it as CodeListSource).codeListData() }
                )

            assertThat(markers).containsExactly(
                "simulated_codelist_0"
            )
        }

        @Test
        fun `Should have diagnostic context info about codelist`() {
            val codeLists = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()
            assertThat(codeLists.size).isEqualTo(1)

            assertThat(codeLists[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelist)
            assertThat(codeLists[0].contextLabel()).isEqualTo("")
            assertThat(codeLists[0].contextIdentifier()).isEqualTo("")
        }

        @Test
        fun `Should have codepages`() {
            val codesPages =
                dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodePagesData().toList()
            val markers =
                extractMarkerValuesFromJsonData(
                    codesPages,
                    { it -> (it as String) }
                )

            assertThat(markers).containsExactly(
                "simulated_codes_page_0",
                "simulated_codes_page_1"
            )

            assertThat(diagnosticCollector.events).containsExactly(
                "ENTER [InitConfiguration]",
                "EXIT [InitConfiguration]",
                "ENTER [InitUriResolution]",
                "EXIT [InitUriResolution]",
                "ENTER [YclCodesPage]",
                "EXIT [YclCodesPage]",
                "ENTER [YclCodesPage]",
                "EXIT [YclCodesPage]"
            )
        }

        @Test
        fun `Should have extensions`() {
            val extensions = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodelistExtensionSources()
            val markers =
                extractMarkerValuesFromJsonData(
                    extensions,
                    { it -> (it as CodeListExtensionSource).extensionData() }
                )

            assertThat(markers).containsExactly(
                "simulated_extension_0"
            )
        }

        @Test
        fun `Should have diagnostic context info about extension`() {
            val extensions = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodelistExtensionSources()
            assertThat(extensions.size).isEqualTo(1)

            assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelistExtension)
            assertThat(extensions[0].contextLabel()).isEqualTo("")
            assertThat(extensions[0].contextIdentifier()).isEqualTo("")
        }

        @Test
        fun `Should have extension member pages`() {
            val extensionPages =
                dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodelistExtensionSources()[0].yclExtensionMemberPagesData()
                    .toList()
            val markers =
                extractMarkerValuesFromJsonData(
                    extensionPages,
                    { it -> it as String }
                )

            assertThat(markers).containsExactly(
                "simulated_extension_memberpage_0",
                "simulated_extension_memberpage_1"
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

    @Nested
    @DisplayName("providing errors")
    inner class ErrorResponses {

        @ParameterizedTest(name = "Should handle communication timeouts during {0} phase")
        @EnumSource(value = SimulationPhase::class, mode = EnumSource.Mode.MATCH_ANY, names = arrayOf(".*"))
        fun `Server communication timeouts at`(phase: SimulationPhase) {
            configureHoverflySimulation(
                mapOf(
                    phase to SimulationVariety.DELAY_RESPONSE
                )
            )

            val thrown = catchThrowable {
                val codelistSource = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0]

                codelistSource.yclCodeSchemeData()
                codelistSource.yclCodePagesData().toList()

                val codelistExtensionSource = codelistSource.yclCodelistExtensionSources()[0]

                codelistExtensionSource.yclExtensionData()
                codelistExtensionSource.yclExtensionMemberPagesData().toList()
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

    private fun yclSourceConfig(): String {
        val config = """
        {
          "dpmDictionaries": [
            {
              "owner": {
                "name": "OwnerName",
                "namespace": "OwnerNamespace",
                "prefix": "OwnerPrefix",
                "location": "OwnerLocation",
                "copyright": "OwnerCopyright",
                "languages": [
                  "en",
                  "fi"
                ],
                "defaultLanguage": "en"
              },
              "codelists": [
                {
                  "uri": "http://uri.suomi.fi/codelist/ytitaxgenfixtures/minimal_zero",
                  "domainCode": "m_zero_override"
                }
              ]
            }
          ]
        }
        """.trimIndent()

        return config
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
                //URI redirects to YCL service
                .redirectGet(
                    currentPhase = SimulationPhase.URL_RESOLUTION_URI_REDIRECT,
                    varietyConf = varietyConf,
                    requestPath = "/codelist/ytitaxgenfixtures/minimal_zero",
                    toTarget = "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero"
                ),

            service("koodistot.suomi.fi")
                //YCL service responses for redirected URI
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

                //YCL service responses for expanded CodeScheme (resolution phase)
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

                //YCL service responses for expanded CodeScheme
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

                //YCL service responses for Code pages
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

                //YCL service responses for expanded Extension
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

                //YCL service responses for Extension Member pages
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
                            "marker": "simulated_extension_memberpage_0"
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
                            "marker": "simulated_extension_memberpage_1"
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
