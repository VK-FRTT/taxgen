package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.HttpOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl.YclSourceBundle
import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.SimulationSource
import io.specto.hoverfly.junit.dsl.HoverflyDsl.response
import io.specto.hoverfly.junit.dsl.HoverflyDsl.service
import io.specto.hoverfly.junit.dsl.ResponseCreators.success
import io.specto.hoverfly.junit.dsl.StubServiceBuilder
import io.specto.hoverfly.junit5.HoverflyExtension
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.byLessThan
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.temporal.ChronoUnit

@DisplayName("when bundle contents are read from simulated YCL service")
@ExtendWith(HoverflyExtension::class)
internal class YclSourceBundle_ApiSimulation_UnitTest(private val hoverfly: Hoverfly) : SourceBundle_UnitTestBase() {

    @Nested
    @DisplayName("which provides successful responses")
    inner class SuccessResponses {

        private lateinit var sourceBundle: SourceBundle

        @BeforeEach
        fun init() {
            hoverflyCustomiseHttpClientTrust()
            hoverflyConfigureSimulation()

            val yclSourceConfig =
                """
                    {
                      "type": "YclSourceConfig",
                      "schemaVersion": 1,
                      "taxonomyUnits": [
                        {
                          "name": "the name",
                          "namespace": "the namespace",
                          "prefix": "the prefix",
                          "location": "the location",
                          "copyright": "the copyright",
                          "supportedLanguages": [
                            "en",
                            "fi"
                          ],
                          "codeLists": [
                            {
                              "uri": "http://uri.suomi.fi/codelist/ytitaxgenfixtures/minimal_zero"
                            },
                            {
                              "uri": "http://uri.suomi.fi/codelist/ytitaxgenfixtures/minimal_one"
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()

            sourceBundle = YclSourceBundle(sourceConfigData = yclSourceConfig)
        }

        @AfterEach
        fun teardown() {
            sourceBundle.close()
        }

        @Test
        fun `Should have bundleinfo @ root`() {
            val infoJson = objectMapper.readTree(sourceBundle.bundleInfoData())
            assertThat(infoJson.isObject).isTrue()

            val createdAt = Instant.parse(infoJson.get("createdAt").textValue())
            assertThat(createdAt).isCloseTo(Instant.now(), byLessThan(30, ChronoUnit.SECONDS))
        }

        @Test
        fun `Should have taxonomyunits @ root # taxonomyunit`() {
            val taxonomyUnits = sourceBundle.taxonomyUnits()
            assertThat(taxonomyUnits.size).isEqualTo(1)

            val infoJson = objectMapper.readTree(
                sourceBundle.taxonomyUnits()[0].taxonomyUnitInfoData()
            )

            assertThat(infoJson.isObject).isTrue()
            assertThat(infoJson.get("name").textValue()).isEqualTo("the name")
            assertThat(infoJson.get("namespace").textValue()).isEqualTo("the namespace")
            assertThat(infoJson.get("prefix").textValue()).isEqualTo("the prefix")
            assertThat(infoJson.get("location").textValue()).isEqualTo("the location")
            assertThat(infoJson.get("copyright").textValue()).isEqualTo("the copyright")
            assertThat(infoJson.get("supportedLanguages").isArray).isTrue()
            assertThat(infoJson.get("supportedLanguages")[0].textValue()).isEqualTo("en")
            assertThat(infoJson.get("supportedLanguages")[1].textValue()).isEqualTo("fi")
        }

        @Test
        fun `Should have codelists @ root # taxonomyunit # codelist`() {
            val codeLists = sourceBundle.taxonomyUnits()[0].codeLists()
            val markers =
                extractMarkerValuesFromJsonData(
                    codeLists,
                    { it -> (it as CodeList).codeListData() }
                )

            assertThat(markers).containsExactly(
                "simulated_codelist_0",
                "simulated_codelist_1"
            )
        }

        @Test
        fun `Should have codepages @ root # taxonomyunit # codelist`() {
            val codesPages = sourceBundle.taxonomyUnits()[0].codeLists()[0].codePagesData().asSequence().toList()
            val markers =
                extractMarkerValuesFromJsonData(
                    codesPages,
                    { it -> (it as String) }
                )

            assertThat(markers).containsExactly(
                "simulated_codepage_0",
                "simulated_codepage_1"
            )
        }
    }

    private fun hoverflyCustomiseHttpClientTrust() {
        val sslConfigurer = hoverfly.sslConfigurer

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslConfigurer.sslContext.socketFactory, sslConfigurer.trustManager)
            .build()

        HttpOps.useHttpClient(okHttpClient)
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
