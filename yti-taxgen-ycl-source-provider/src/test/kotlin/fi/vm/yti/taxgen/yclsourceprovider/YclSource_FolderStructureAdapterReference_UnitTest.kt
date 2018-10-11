package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("when ycl sources are read from reference folder structure")
internal class YclSource_FolderStructureAdapterReference_UnitTest : YclSource_UnitTestBase() {

    private lateinit var yclSource: YclSource
    private lateinit var resourcePath: Path

    @BeforeEach
    fun init() {
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceUri =
            classLoader.getResource("folder_structure_adapter_reference").toURI()
        resourcePath = Paths.get(resourceUri)
        yclSource = YclSourceFolderStructureAdapter(resourcePath)
    }

    @AfterEach
    fun teardown() {
        yclSource.close()
    }

    @Test
    fun `Should have source config at root`() {
        val configJson = objectMapper.readTree(yclSource.sourceConfigData())

        assertThat(configJson.isObject).isTrue()
        assertThat(configJson.get("marker") as Any).isNotNull()
        assertThat(configJson.get("marker").textValue()).isEqualTo("folder_structure_adapter_reference/meta/source_config")
    }

    @Test
    fun `Should have diagnostic context info about yclsource`() {
        assertThat(yclSource.contextType()).isEqualTo(DiagnosticContextType.YclSource)
        assertThat(yclSource.contextLabel()).isEqualTo("folder")
        assertThat(yclSource.contextIdentifier()).isEqualTo(resourcePath.toString())
    }

    @Test
    fun `Should have owner config`() {
        val dpmDictionarySources = yclSource.dpmDictionarySources()
        val markers =
            extractMarkerValuesFromJsonData(
                dpmDictionarySources,
                { it -> (it as DpmDictionarySource).dpmOwnerConfigData() }
            )

        assertThat(markers).containsExactly(
            "dpmdictionary_0/owner_info",
            "dpmdictionary_1/owner_info",
            "dpmdictionary_2/owner_info",
            "dpmdictionary_3/owner_info",
            "dpmdictionary_4/owner_info",
            "dpmdictionary_5/owner_info",
            "dpmdictionary_6/owner_info",
            "dpmdictionary_7/owner_info",
            "dpmdictionary_8/owner_info",
            "dpmdictionary_9/owner_info",
            "dpmdictionary_10/owner_info",
            "dpmdictionary_11/owner_info"
        )
    }

    @Test
    fun `Should have diagnostic context info about dpmdictionary`() {
        val dpmDictionarySources = yclSource.dpmDictionarySources()
        assertThat(dpmDictionarySources.size).isEqualTo(12)

        assertThat(dpmDictionarySources[0].contextType()).isEqualTo(DiagnosticContextType.DpmDictionary)
        assertThat(dpmDictionarySources[0].contextLabel()).isEqualTo("")
        assertThat(dpmDictionarySources[0].contextIdentifier()).isEqualTo("")
        assertThat(dpmDictionarySources[11].contextIdentifier()).isEqualTo("")
    }

    @Test
    fun `Should have codelist source config`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        val markers =
            extractMarkerValuesFromJsonData(
                codeLists,
                { it -> (it as YclCodelistSource).yclCodelistSourceConfigData() }
            )

        assertThat(markers).containsExactly(
            "dpmdictionary_0/codelist_0/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_1/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_2/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_3/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_4/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_5/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_6/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_7/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_8/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_9/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_10/ycl_codelist_source_config",
            "dpmdictionary_0/codelist_11/ycl_codelist_source_config"
        )
    }

    @Test
    fun `Should have codelists`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        val markers =
            extractMarkerValuesFromJsonData(
                codeLists,
                { it -> (it as YclCodelistSource).yclCodeSchemeData() }
            )

        assertThat(markers).containsExactly(
            "dpmdictionary_0/codelist_0/ycl_codescheme",
            "dpmdictionary_0/codelist_1/ycl_codescheme",
            "dpmdictionary_0/codelist_2/ycl_codescheme",
            "dpmdictionary_0/codelist_3/ycl_codescheme",
            "dpmdictionary_0/codelist_4/ycl_codescheme",
            "dpmdictionary_0/codelist_5/ycl_codescheme",
            "dpmdictionary_0/codelist_6/ycl_codescheme",
            "dpmdictionary_0/codelist_7/ycl_codescheme",
            "dpmdictionary_0/codelist_8/ycl_codescheme",
            "dpmdictionary_0/codelist_9/ycl_codescheme",
            "dpmdictionary_0/codelist_10/ycl_codescheme",
            "dpmdictionary_0/codelist_11/ycl_codescheme"
        )
    }

    @Test
    fun `Should have diagnostic context info about codelist`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        assertThat(codeLists.size).isEqualTo(12)

        assertThat(codeLists[0].contextType()).isEqualTo(DiagnosticContextType.YclCodelist)
        assertThat(codeLists[0].contextLabel()).isEqualTo("")
        assertThat(codeLists[0].contextIdentifier()).isEqualTo("")
        assertThat(codeLists[11].contextIdentifier()).isEqualTo("")
    }

    @Test
    fun `Should have codepages`() {
        val codesPages =
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodePagesData().toList()
        val markers =
            extractMarkerValuesFromJsonData(
                codesPages,
                { it -> it as String }
            )

        assertThat(markers).containsExactly(
            "dpmdictionary_0/codelist_0/codes_page_0",
            "dpmdictionary_0/codelist_0/codes_page_1",
            "dpmdictionary_0/codelist_0/codes_page_2",
            "dpmdictionary_0/codelist_0/codes_page_3",
            "dpmdictionary_0/codelist_0/codes_page_4",
            "dpmdictionary_0/codelist_0/codes_page_5",
            "dpmdictionary_0/codelist_0/codes_page_6",
            "dpmdictionary_0/codelist_0/codes_page_7",
            "dpmdictionary_0/codelist_0/codes_page_8",
            "dpmdictionary_0/codelist_0/codes_page_9",
            "dpmdictionary_0/codelist_0/codes_page_10",
            "dpmdictionary_0/codelist_0/codes_page_11"
        )
    }

    @Test
    fun `Should have extensions`() {
        val extensions = yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodelistExtensionSources()
        val markers =
            extractMarkerValuesFromJsonData(
                extensions,
                { it -> (it as YclCodelistExtensionSource).yclExtensionData() }
            )

        assertThat(markers).containsExactly(
            "dpmdictionary_0/codelist_0/extension_0",
            "dpmdictionary_0/codelist_0/extension_1",
            "dpmdictionary_0/codelist_0/extension_2",
            "dpmdictionary_0/codelist_0/extension_3",
            "dpmdictionary_0/codelist_0/extension_4",
            "dpmdictionary_0/codelist_0/extension_5",
            "dpmdictionary_0/codelist_0/extension_6",
            "dpmdictionary_0/codelist_0/extension_7",
            "dpmdictionary_0/codelist_0/extension_8",
            "dpmdictionary_0/codelist_0/extension_9",
            "dpmdictionary_0/codelist_0/extension_10",
            "dpmdictionary_0/codelist_0/extension_11"
        )
    }

    @Test
    fun `Should have diagnostic context info about extension`() {
        val extensions = yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodelistExtensionSources()
        assertThat(extensions.size).isEqualTo(12)

        assertThat(extensions[0].contextType()).isEqualTo(DiagnosticContextType.YclCodelistExtension)
        assertThat(extensions[0].contextLabel()).isEqualTo("")
        assertThat(extensions[0].contextIdentifier()).isEqualTo("")
        assertThat(extensions[11].contextIdentifier()).isEqualTo("")
    }

    @Test
    fun `Should have extension member pages`() {
        val extensionPages =
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodelistExtensionSources()[0].yclExtensionMemberPagesData()
                .toList()
        val markers =
            extractMarkerValuesFromJsonData(
                extensionPages,
                { it -> it as String }
            )

        assertThat(markers).containsExactly(
            "dpmdictionary_0/codelist_0/extension_0/memberpage_0",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_1",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_2",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_3",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_4",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_5",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_6",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_7",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_8",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_9",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_10",
            "dpmdictionary_0/codelist_0/extension_0/memberpage_11"
        )
    }
}
