package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.YclSourceFolderStructureRecorder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("When ycl sources are recorded to folder and then read back")
internal class DpmSource_FolderStructureLoopback_UnitTest : DpmSource_UnitTestBase() {

    private lateinit var tempFolder: TempFolder

    private lateinit var dpmSource: DpmSource

    @BeforeEach
    fun init() {
        tempFolder = TempFolder("yclsource_folder_structure_loopback")

        YclSourceFolderStructureRecorder(
            baseFolderPath = tempFolder.path(),
            forceOverwrite = false,
            diagnostic = diagnostic
        ).use {
            it.captureSources(FixedDpmSource())
        }

        dpmSource = DpmSourceFolderAdapter(tempFolder.path())
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
    fun `Should have codelists`() {
        val codeLists = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()
        val markers =
            extractMarkerValuesFromJsonData(
                codeLists,
                { it -> (it as CodeListSource).codeListData() }
            )

        assertThat(markers).containsExactly(
            "fixed_codescheme_d0_c0",
            "fixed_codescheme_d0_c1"
        )
    }

    @Test
    fun `Should have codepages`() {
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
    fun `Should have extensions`() {
        val extensions = dpmSource.dpmDictionarySources()[0].explicitDomainAndHierarchiesSources()[0].yclCodelistExtensionSources()
        val markers =
            extractMarkerValuesFromJsonData(
                extensions,
                { it -> (it as CodeListExtensionSource).extensionData() }
            )

        assertThat(markers).containsExactly(
            "fixed_extension_d0_c0_e0",
            "fixed_extension_d0_c0_e1"
        )
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
