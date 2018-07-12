package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureRecorder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

@DisplayName("When ycl sources are recorded to folder and then read back")
internal class YclSource_FolderStructureLoopback_UnitTest : YclSource_UnitTestBase() {

    private lateinit var targetFolderPath: Path
    private lateinit var yclSource: YclSource

    @BeforeEach
    fun init() {
        targetFolderPath = Files.createTempDirectory("folder_structure_loopback")

        YclSourceFolderStructureRecorder(
            baseFolderPath = targetFolderPath,
            yclSource = FixedYclSource(),
            forceOverwrite = false
        ).use {
            it.capture()
        }

        yclSource = YclSourceFolderStructureAdapter(targetFolderPath)
    }

    @AfterEach
    fun teardown() {
        yclSource.close()

        Files
            .walk(targetFolderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun `Should have source info at root`() {
        val infoJson = objectMapper.readTree(yclSource.sourceInfoData())

        assertThat(infoJson.isObject).isTrue()
        assertThat(infoJson.get("marker") as Any).isNotNull()
        assertThat(infoJson.get("marker").textValue()).isEqualTo("fixed_source_info")
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
            yclSource.dpmDictionarySources()[0].yclCodelistSources()[0].yclCodePagesData().asSequence().toList()
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
