package fi.vm.yti.taxgen.yclsourceprovider

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
    fun `Should have source info at root`() {
        val infoJson = objectMapper.readTree(yclSource.sourceInfoData())

        assertThat(infoJson.isObject).isTrue()
        assertThat(infoJson.get("marker") as Any).isNotNull()
        assertThat(infoJson.get("marker").textValue()).isEqualTo("folder_structure_adapter_reference/source_info")
    }

    @Test
    fun `Should have diagnostic topic info about yclsource @ root`() {
        assertThat(yclSource.topicType()).isEqualTo("Reading YCL Sources")
        assertThat(yclSource.topicName()).isEqualTo("folder")
        assertThat(yclSource.topicIdentifier()).isEqualTo(resourcePath.toString())
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
    fun `Should have diagnostic topic info about dpmdictionary @ root # dpmdictionary`() {
        val dpmDictionarySources = yclSource.dpmDictionarySources()
        assertThat(dpmDictionarySources.size).isEqualTo(12)

        assertThat(dpmDictionarySources[0].topicType()).isEqualTo("DPM Dictionary")
        assertThat(dpmDictionarySources[0].topicName()).isEqualTo("")
        assertThat(dpmDictionarySources[0].topicIdentifier()).isEqualTo("#0")
        assertThat(dpmDictionarySources[11].topicIdentifier()).isEqualTo("#11")
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
    fun `Should have diagnostic topic info about codelist @ root # dpmdictionary # codelist`() {
        val codeLists = yclSource.dpmDictionarySources()[0].yclCodelistSources()
        assertThat(codeLists.size).isEqualTo(12)

        assertThat(codeLists[0].topicType()).isEqualTo("Codelist")
        assertThat(codeLists[0].topicName()).isEqualTo("")
        assertThat(codeLists[0].topicIdentifier()).isEqualTo("#0")
        assertThat(codeLists[11].topicIdentifier()).isEqualTo("#11")
    }

    @Test
    fun `Should have codepages @ root # dpmdictionary # codelist`() {
        val codesPages =
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodePagesData().asSequence().toList()
        val markers =
            extractMarkerValuesFromJsonData(
                codesPages,
                { it -> it as String }
            )

        assertThat(markers).containsExactly(
            "dpmdictionary_0/codelist_0/codepage_0",
            "dpmdictionary_0/codelist_0/codepage_1",
            "dpmdictionary_0/codelist_0/codepage_2",
            "dpmdictionary_0/codelist_0/codepage_3",
            "dpmdictionary_0/codelist_0/codepage_4",
            "dpmdictionary_0/codelist_0/codepage_5",
            "dpmdictionary_0/codelist_0/codepage_6",
            "dpmdictionary_0/codelist_0/codepage_7",
            "dpmdictionary_0/codelist_0/codepage_8",
            "dpmdictionary_0/codelist_0/codepage_9",
            "dpmdictionary_0/codelist_0/codepage_10",
            "dpmdictionary_0/codelist_0/codepage_11"
        )
    }
}
