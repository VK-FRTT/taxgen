package fi.vm.yti.taxgen.yclsourceprovider

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.yclsourceprovider.config.OwnerConfig
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.yclsourceprovider.api.YclSourceApiAdapter
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps
import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.SimulationSource
import io.specto.hoverfly.junit.dsl.HoverflyDsl.response
import io.specto.hoverfly.junit.dsl.HoverflyDsl.service
import io.specto.hoverfly.junit.dsl.ResponseCreators.success
import io.specto.hoverfly.junit.dsl.StubServiceBuilder
import io.specto.hoverfly.junit5.HoverflyExtension
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Path

@DisplayName("when ycl sources are read from simulated YCL API")
@ExtendWith(HoverflyExtension::class)
internal class YclSource_ApiAdapterSimulation_UnitTest(private val hoverfly: Hoverfly) : YclSource_UnitTestBase() {

    private lateinit var tempFolder: TempFolder
    private lateinit var configFilePath: Path

    @Nested
    @DisplayName("providing successful responses")
    inner class SuccessResponses {

        private lateinit var yclSource: YclSource

        @BeforeEach
        fun init() {
            tempFolder = TempFolder("yclsource_apiadapter_unittest")

            hoverflyCustomiseHttpClientTrust()
            hoverflyConfigureSimulation()

            val yclSourceConfig =
                """
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
                        },
                        {
                          "uri": "http://uri.suomi.fi/codelist/ytitaxgenfixtures/minimal_one",
                          "domainCode": "m_one_override"
                        }
                      ]
                    }
                  ]
                }
                """.trimIndent()

            configFilePath = tempFolder.createFileWithContent("ycl_source_config.json", yclSourceConfig)
            yclSource = YclSourceApiAdapter(configFilePath, diagnostic)
        }

        @AfterEach
        fun teardown() {
            yclSource.close()
            tempFolder.close()
        }

        @Test
        fun `Should have source config at root`() {
            val configJson = objectMapper.readTree(yclSource.sourceConfigData())
            assertThat(configJson.isObject).isTrue()

            assertThat(configJson.at("/dpmDictionaries/0/owner/name").asText()).isEqualTo("OwnerName")
            assertThat(configJson.at("/dpmDictionaries/0/codelists/0/domainCode").asText()).isEqualTo("m_zero_override")
        }

        @Test
        fun `Should have diagnostic context info about yclsource @ root`() {
            assertThat(yclSource.contextType()).isEqualTo(DiagnosticContextType.YclSource)
            assertThat(yclSource.contextName()).isEqualTo("YTI Reference Data service")
            assertThat(yclSource.contextRef()).isEqualTo(configFilePath.toString())
        }

        @Test
        fun `Should have owner config @ root # dpmdictionary`() {
            val dpmDictionarySources = yclSource.dpmDictionarySources()
            assertThat(dpmDictionarySources.size).isEqualTo(1)

            val ownerConfig = objectMapper.readValue<OwnerConfig>(
                yclSource.dpmDictionarySources()[0].dpmOwnerConfigData()
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
        fun `Should have diagnostic context info about dpmdictionary @ root # dpmdictionary`() {
            val dpmDictionarySources = yclSource.dpmDictionarySources()
            assertThat(dpmDictionarySources.size).isEqualTo(1)

            assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
            assertThat(dpmDictionarySources[0].contextName()).isEqualTo("")
            assertThat(dpmDictionarySources[0].contextRef()).isEqualTo("#0")
        }

        @Test
        fun `Should have codelist source config @ root # dpmdictionary # codelist`() {
            val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()

            val configJson0 = objectMapper.readTree(codeLists[0].yclCodelistSourceConfigData())
            assertThat(configJson0.isObject).isTrue()
            assertThat(configJson0.get("domainCode").textValue()).isEqualTo("m_zero_override")

            val configJson1 = objectMapper.readTree(codeLists[1].yclCodelistSourceConfigData())
            assertThat(configJson1.isObject).isTrue()
            assertThat(configJson1.get("domainCode").textValue()).isEqualTo("m_one_override")
        }

        @Test
        fun `Should have codelists @ root # dpmdictionary # codelist`() {
            val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
            val markers =
                extractMarkerValuesFromJsonData(
                    codeLists,
                    { it -> (it as YclCodelistSource).yclCodeschemeData() }
                )

            assertThat(markers).containsExactly(
                "simulated_codelist_0",
                "simulated_codelist_1"
            )
        }

        @Test
        fun `Should have diagnostic context info about codelist @ root # dpmdictionary # codelist`() {
            val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
            assertThat(codeLists.size).isEqualTo(2)

            assertThat(codeLists[0].contextType()).isEqualTo(DiagnosticContextType.YclCodelist)
            assertThat(codeLists[0].contextName()).isEqualTo("")
            assertThat(codeLists[0].contextRef()).isEqualTo("#0")
        }

        @Test
        fun `Should have codepages @ root # dpmdictionary # codelist`() {
            val codesPages =
                yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodePagesData().toList()
            val markers =
                extractMarkerValuesFromJsonData(
                    codesPages,
                    { it -> (it as String) }
                )

            assertThat(markers).containsExactly(
                "simulated_codepage_0",
                "simulated_codepage_1"
            )

            assertThat(diagnosticConsumerCaptor.events).containsExactly(
                "ENTER [InitConfiguration]",
                "EXIT [] RETIRED [InitConfiguration]",
                "ENTER [InitUriResolution]",
                "EXIT [] RETIRED [InitUriResolution]",
                "ENTER [YclCodesPage]",
                "EXIT [] RETIRED [YclCodesPage]",
                "ENTER [YclCodesPage]",
                "EXIT [] RETIRED [YclCodesPage]"
            )
        }
    }

    private fun hoverflyCustomiseHttpClientTrust() {
        val sslConfigurer = hoverfly.sslConfigurer

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslConfigurer.sslContext.socketFactory, sslConfigurer.trustManager)
            .build()

        HttpOps.useCustomHttpClient(okHttpClient)
    }

    private fun hoverflyConfigureSimulation() {
        val simulationSource = SimulationSource.dsl(
            service("uri.suomi.fi")
                //URI redirects to YCL service
                .redirectGet(
                    requestPath = "/codelist/ytitaxgenfixtures/minimal_zero",
                    toTarget = "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero"
                )
                .redirectGet(
                    requestPath = "/codelist/ytitaxgenfixtures/minimal_one",
                    toTarget = "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_one"
                ),

            service("koodistot.suomi.fi")
                //YCL service responses for redirected URI
                .respondGetWithJson(
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero",
                    responseJson = """
                        {
                            "url":"http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero",
                            "codesUrl":"http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/codes/"
                        }
                        """.trimIndent()
                )

                .respondGetWithJson(
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_one",
                    responseJson = """
                        {
                            "url":"http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_one",
                            "codesUrl":"http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_one/codes/"
                        }
                        """.trimIndent()
                )

                //YCL service responses for expanded CodeScheme
                .respondGetWithJson(
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero",
                    queryParams = listOf(Pair("expand", "code")),
                    responseJson = """
                        {
                            "marker": "simulated_codelist_0"
                        }
                        """.trimIndent()
                )

                .respondGetWithJson(
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_one",
                    queryParams = listOf(Pair("expand", "code")),
                    responseJson = """
                        {
                            "marker": "simulated_codelist_1"
                        }
                        """.trimIndent()
                )

                //YCL service responses for Code pages
                .respondGetWithJson(
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero/codes/",
                    queryParams = listOf(Pair("pageSize", "1000")),
                    responseJson = """
                        {
                            "meta": {
                                "nextPage": "http://koodistot.suomi.fi/api/codelist/ytitaxgenfixtures_minimal_zero/codes/?pageSize=1000&from=1000"
                            },
                            "marker": "simulated_codepage_0"
                        }
                        """.trimIndent()
                )

                .respondGetWithJson(
                    requestPath = "/api/codelist/ytitaxgenfixtures_minimal_zero/codes/",
                    queryParams = listOf(Pair("pageSize", "1000"), Pair("from", "1000")),
                    responseJson = """
                        {
                            "meta": {
                                "nextPage": null
                            },
                            "marker": "simulated_codepage_1"
                        }
                        """.trimIndent()
                )
        )

        hoverfly.simulate(simulationSource)
    }

    private fun StubServiceBuilder.redirectGet(
        requestPath: String,
        toTarget: String
    ): StubServiceBuilder {
        val requestMatcherBuilder = get(requestPath)

        requestMatcherBuilder.willReturn(
            response()
                .status(303)
                .header("Location", toTarget)
        )

        return this
    }

    private fun StubServiceBuilder.respondGetWithJson(
        requestPath: String,
        queryParams: List<Pair<String, String>>? = null,
        responseJson: String
    ): StubServiceBuilder {
        val requestMatcherBuilder = get(requestPath)

        queryParams?.forEach { it ->
            requestMatcherBuilder.queryParam(it.first, it.second)
        }

        requestMatcherBuilder.willReturn(success(responseJson, "application/responseJson"))

        return this
    }
}
