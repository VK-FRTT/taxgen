package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceZipFileAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.YclSourceZipFileRecorder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path

@DisplayName("When ycl sources are recorded to zip file and then read back from the file")
internal class DpmSource_ZipFileLoopback_UnitTest : DpmSource_UnitTestBase() {

    private lateinit var tempFolder: TempFolder
    private lateinit var targetZipPath: Path
    private lateinit var dpmSource: DpmSource

    @BeforeEach
    fun init() {
        tempFolder = TempFolder("yclsource_zip_file_loopback")
        targetZipPath = tempFolder.resolve("file.zip")

        YclSourceZipFileRecorder(
            targetZipPath = targetZipPath,
            forceOverwrite = false,
            diagnostic = diagnostic
        ).use {
            it.captureSources(FixedDpmSource())
        }

        dpmSource = DpmSourceZipFileAdapter(targetZipPath)
    }

    @AfterEach
    fun teardown() {
        dpmSource.close()
        tempFolder.close()
    }

    @Test
    fun `Should have source config at root`() {
        val configJson = objectMapper.readTree(dpmSource.sourceConfigData())

        assertThat(configJson.isObject).isTrue()
        assertThat(configJson.get("marker") as Any).isNotNull()
        assertThat(configJson.get("marker").textValue()).isEqualTo("fixed_source_config")
    }

    @Test
    fun `Should have diagnostic context info about yclsource`() {
        assertThat(dpmSource.contextType()).isEqualTo(DiagnosticContextType.DpmSource)
        assertThat(dpmSource.contextLabel()).isEqualTo("ZIP file")
        assertThat(dpmSource.contextIdentifier()).isEqualTo(targetZipPath.toString())
    }

    @Test
    fun `Should have owner config`() {
        val dpmDictionarySources = dpmSource.dpmDictionarySources()
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
    fun `Should have diagnostic context info about dpmdictionary`() {
        val dpmDictionarySources = dpmSource.dpmDictionarySources()
        assertThat(dpmDictionarySources.size).isEqualTo(2)

        assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
        assertThat(dpmDictionarySources[0].contextLabel()).isEqualTo("")
        assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
    }

    @Test
    fun `Should have codelists`() {
        val codeLists = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()
        val markers = extractMarkerValuesFromJsonData(
            codeLists,
            { it -> (it as CodeListSource).codeListData() }
        )

        assertThat(markers).containsExactly(
            "fixed_codescheme_d0_c0",
            "fixed_codescheme_d0_c1"
        )
    }

    @Test
    fun `Should have diagnostic context info about codelist`() {
        val codeLists = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()
        assertThat(codeLists.size).isEqualTo(2)

        assertThat(codeLists[0].contextType()).isEqualTo(DiagnosticContextType.RdsCodelist)
        assertThat(codeLists[0].contextLabel()).isEqualTo("")
        assertThat(codeLists[0].contextIdentifier()).isEqualTo("")
        assertThat(codeLists[1].contextIdentifier()).isEqualTo("")
    }

    @Test
    fun `Should have codepages`() {
        val codesPages =
            dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodePagesData().toList()
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
    fun `Should have extensions`() {
        val codesPages =
            dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodePagesData().toList()
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
    fun `Should have diagnostic context info about extension`() {
        val extensions = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodelistExtensionSources()
        assertThat(extensions.size).isEqualTo(2)

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
            "fixed_extension_member_d0_c0_e0_p0",
            "fixed_extension_member_d0_c0_e0_p1"
        )
    }
}
