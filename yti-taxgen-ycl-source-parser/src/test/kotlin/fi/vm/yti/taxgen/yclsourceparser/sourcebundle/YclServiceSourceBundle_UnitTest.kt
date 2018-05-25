package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.HttpOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.YclServiceSourceBundle
import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit5.HoverflyExtension
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(HoverflyExtension::class)
@DisplayName("when bundle contents are read from simulated YCL service")
internal class YclServiceSourceBundle_UnitTest(private val hoverfly: Hoverfly) {

    private val objectMapper = jacksonObjectMapper()

    private lateinit var sourceBundle: SourceBundle

    @BeforeEach
    fun init() {
        customiseOkHttpClient()

        val resourceUri = this::class.java.getResource("yclservicesourcebundle_unittest/ycl_source_config.json").toURI()
        val resourcePath = Paths.get(resourceUri)

        sourceBundle = YclServiceSourceBundle(resourcePath)
    }

    @AfterEach
    fun teardown() {
        sourceBundle.close()
    }

    @Test
    fun `Should have bundleinfo @ root`() {
        val infoJson = objectMapper.readTree(sourceBundle.bundleInfoData())
    }

    @Test
    fun `Should have taxonomyunits @ root # taxonomyunit`() {
        val taxonomyUnits = sourceBundle.taxonomyUnits()
        assertThat(taxonomyUnits.size).isEqualTo(1)

        val infoJson = objectMapper.readTree(sourceBundle.taxonomyUnits()[0].taxonomyUnitInfoData())

        assertThat(infoJson.isObject).isTrue()
        assertThat(infoJson.get("namespace").textValue()).isEqualTo("yclsb_unittest_namespace")
        assertThat(infoJson.get("namespacePrefix").textValue()).isEqualTo("yclsb_unittest_namespacePrefix")
        assertThat(infoJson.get("officialLocation").textValue()).isEqualTo("yclsb_unittest_officialLocation")
        assertThat(infoJson.get("copyrightText").textValue()).isEqualTo("yclsb_unittest_copyrightText")
        assertThat(infoJson.get("supportedLanguages").isArray).isTrue()
        assertThat(infoJson.get("supportedLanguages")[0].textValue()).isEqualTo("en")
        assertThat(infoJson.get("supportedLanguages")[1].textValue()).isEqualTo("fi")
    }

    @Test
    fun `Should have codelists @ root # taxonomyunit # codelist`() {
        //val codeLists = sourceBundle.taxonomyUnits()[0].codeLists()
        //assertThat(codeLists.size).isEqualTo(1)
    }

    @Test
    fun `Should have codepages @ root # taxonomyunit # codelist`() {
        //val codesPages = sourceBundle.taxonomyUnits()[0].codeLists()[0].codePagesData().asSequence().toList()
        //assertThat(codesPages.size).isEqualTo(1)
    }

    private fun customiseOkHttpClient() {
        val sslConfigurer = hoverfly.sslConfigurer

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslConfigurer.sslContext.socketFactory, sslConfigurer.trustManager)
            .build()

        HttpOps.useHttpClient(okHttpClient)
    }
}
