package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.yclsourceprovider.zip.YclSourceZipFileAdapter
import fi.vm.yti.taxgen.yclsourceprovider.zip.YclSourceZipFileRecorder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path

@DisplayName("When ycl sources are recorded to zip file and then read back from the file")
internal class YclSource_ZipFileLoopback_UnitTest : YclSource_UnitTestBase() {

    private lateinit var tempFolder: TempFolder
    private lateinit var targetZipPath: Path
    private lateinit var yclSource: YclSource

    @BeforeEach
    fun init() {
        tempFolder = TempFolder("yclsource_zip_file_loopback")
        targetZipPath = tempFolder.resolve("file.zip")

        YclSourceZipFileRecorder(
            targetZipPath = targetZipPath,
            forceOverwrite = false,
            diagnostic = diagnostic
        ).use {
            it.captureSources(FixedYclSource())
        }

        yclSource = YclSourceZipFileAdapter(targetZipPath)
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
        assertThat(configJson.get("marker") as Any).isNotNull()
        assertThat(configJson.get("marker").textValue()).isEqualTo("fixed_source_config")
    }

    @Test
    fun `Should have diagnostic context info about yclsource {@ root}`() {
        assertThat(yclSource.contextType()).isEqualTo(DiagnosticContextType.YclSource)
        assertThat(yclSource.contextName()).isEqualTo("ZIP file")
        assertThat(yclSource.contextRef()).isEqualTo(targetZipPath.toString())
    }

    @Test
    fun `Should have owner config {@ root # dpmdictionary}`() {
        val dpmDictionarySources = yclSource.dpmDictionarySources()
        val markers =
            extractMarkerValuesFromJsonData(
                dpmDictionarySources,
                { it -> (it as DpmDictionarySource).dpmOwnerConfigData() }
            )

        assertThat(markers).containsExactly(
            "fixed_dpm_owner_config_d0",
            "fixed_dpm_owner_config_d1"
        )
    }

    @Test
    fun `Should have diagnostic context info about dpmdictionary {@ root # dpmdictionary}`() {
        val dpmDictionarySources = yclSource.dpmDictionarySources()
        assertThat(dpmDictionarySources.size).isEqualTo(2)

        assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
        assertThat(dpmDictionarySources[0].contextName()).isEqualTo("")
        assertThat(dpmDictionarySources[0].contextRef()).isEqualTo("")
    }

    @Test
    fun `Should have codelist source config {@ root # dpmdictionary # codelist}`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        val markers =
            extractMarkerValuesFromJsonData(
                codeLists,
                { it -> (it as YclCodelistSource).yclCodelistSourceConfigData() }
            )

        assertThat(markers).containsExactly(
            "fixed_codelist_source_config_d0_c0",
            "fixed_codelist_source_config_d0_c1"
        )
    }

    @Test
    fun `Should have codelists {@ root # dpmdictionary # codelist}`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        val markers = extractMarkerValuesFromJsonData(
            codeLists,
            { it -> (it as YclCodelistSource).yclCodeSchemeData() }
        )

        assertThat(markers).containsExactly(
            "fixed_codescheme_d0_c0",
            "fixed_codescheme_d0_c1"
        )
    }

    @Test
    fun `Should have diagnostic context info about codelist {@ root # dpmdictionary # codelist}`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        assertThat(codeLists.size).isEqualTo(2)

        assertThat(codeLists[0].contextType()).isEqualTo(DiagnosticContextType.YclCodelist)
        assertThat(codeLists[0].contextName()).isEqualTo("")
        assertThat(codeLists[0].contextRef()).isEqualTo("")
        assertThat(codeLists[1].contextRef()).isEqualTo("")
    }

    @Test
    fun `Should have codepages {@ root # dpmdictionary # codelist}`() {
        val codesPages =
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodePagesData().toList()
        val markers = extractMarkerValuesFromJsonData(
            codesPages,
            { it -> it as String }
        )

        assertThat(markers).containsExactly(
            "fixed_codes_page_d0_c0_p0",
            "fixed_codes_page_d0_c0_p1"
        )
    }

    @Test
    fun `Should have extensions {@ root # dpmdictionary # codelist}`() {
        val codesPages =
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodePagesData().toList()
        val markers =
            extractMarkerValuesFromJsonData(
                codesPages,
                { it -> it as String }
            )

        assertThat(markers).containsExactly(
            "fixed_codes_page_d0_c0_p0",
            "fixed_codes_page_d0_c0_p1"
        )
    }

    @Test
    fun `Should have diagnostic context info about extension {@ root # dpmdictionary # codelist # extension}`() {
        val extensions = yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodelistExtensionSources()
        assertThat(extensions.size).isEqualTo(2)

        assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.YclCodelistExtension)
        assertThat(extensions[0].contextName()).isEqualTo("")
        assertThat(extensions[0].contextRef()).isEqualTo("")
    }

    @Test
    fun `Should have extension member pages {@ root # dpmdictionary # codelist # extension}`() {
        val extensionPages =
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodelistExtensionSources()[0].yclExtensionMemberPagesData()
                .toList()
        val markers =
            extractMarkerValuesFromJsonData(
                extensionPages,
                { it -> it as String }
            )

        assertThat(markers).containsExactly(
            "fixed_extension_member_d0_c0_e0_p0",
            "fixed_extension_member_d0_c0_e0_p1"
        )
    }
}
