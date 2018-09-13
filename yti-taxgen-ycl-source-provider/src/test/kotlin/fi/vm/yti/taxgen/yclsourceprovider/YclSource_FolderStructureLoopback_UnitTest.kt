package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureRecorder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("When ycl sources are recorded to folder and then read back")
internal class YclSource_FolderStructureLoopback_UnitTest : YclSource_UnitTestBase() {

    private lateinit var tempFolder: TempFolder

    private lateinit var yclSource: YclSource

    @BeforeEach
    fun init() {
        tempFolder = TempFolder("yclsource_folder_structure_loopback")

        YclSourceFolderStructureRecorder(
            baseFolderPath = tempFolder.path(),
            forceOverwrite = false,
            diagnostic = diagnostic
        ).use {
            it.captureSources(FixedYclSource())
        }

        yclSource = YclSourceFolderStructureAdapter(tempFolder.path())
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
    fun `Should have owner config @ root # dpmdictionary`() {
        val dpmDictionarySources = yclSource.dpmDictionarySources()
        val markers =
            extractMarkerValuesFromJsonData(
                dpmDictionarySources,
                { it -> (it as DpmDictionarySource).dpmOwnerConfigData() }
            )

        assertThat(markers).containsExactly(
            "fixed_dpm_owner_config_0",
            "fixed_dpm_owner_config_1"
        )
    }

    @Test
    fun `Should have codelist source config @ root # dpmdictionary # codelist`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        val markers =
            extractMarkerValuesFromJsonData(
                codeLists,
                { it -> (it as YclCodelistSource).yclCodelistSourceConfigData() }
            )

        assertThat(markers).containsExactly(
            "fixed_codelist_source_config_0",
            "fixed_codelist_source_config_1"
        )
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
            "fixed_codescheme_0",
            "fixed_codescheme_1"
        )
    }

    @Test
    fun `Should have codepages @ root # dpmdictionary # codelist`() {
        val codesPages =
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodePagesData().toList()
        val markers =
            extractMarkerValuesFromJsonData(
                codesPages,
                { it -> it as String }
            )

        assertThat(markers).containsExactly(
            "fixed_codepage_0",
            "fixed_codepage_1"
        )
    }
}
