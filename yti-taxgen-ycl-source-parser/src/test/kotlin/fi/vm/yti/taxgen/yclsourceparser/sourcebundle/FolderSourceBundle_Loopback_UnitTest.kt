package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundleWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

@DisplayName("When bundle contents are written to folder and then read back")
internal class FolderSourceBundle_Loopback_UnitTest : SourceBundle_UnitTestBase() {

    private lateinit var targetFolderPath: Path
    private lateinit var folderSourceBundle: FolderSourceBundle

    @BeforeEach
    fun init() {
        targetFolderPath = Files.createTempDirectory("foldersourcebundle_loopback_unittest")

        val sourceBundle = FixedSourceBundle()

        FolderSourceBundleWriter(
            baseFolderPath = targetFolderPath,
            sourceBundle = sourceBundle,
            forceOverwrite = false
        ).use {
            it.write()
        }

        folderSourceBundle = FolderSourceBundle(targetFolderPath)
    }

    @AfterEach
    fun teardown() {
        folderSourceBundle.close()

        Files
            .walk(targetFolderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun `Should have bundleinfo @ root`() {
        val infoJson = objectMapper.readTree(folderSourceBundle.bundleInfoData())

        assertThat(infoJson.isObject).isTrue()
        assertThat(infoJson.get("marker") as Any).isNotNull()
        assertThat(infoJson.get("marker").textValue()).isEqualTo("fixed_source_bundle")
    }

    @Test
    fun `Should have taxonomyunits @ root # taxonomyunit`() {
        val taxonomyUnits = folderSourceBundle.taxonomyUnits()
        val markers =
            extractMarkerValuesFromJsonData(
                taxonomyUnits,
                { it -> (it as TaxonomyUnit).taxonomyUnitInfoData() }
            )

        assertThat(markers).containsExactly(
            "fixed_taxonomyunit_0",
            "fixed_taxonomyunit_1"
        )
    }

    @Test
    fun `Should have codelists @ root # taxonomyunit # codelist`() {
        val codeLists = folderSourceBundle.taxonomyUnits()[0].codeLists()
        val markers =
            extractMarkerValuesFromJsonData(
                codeLists,
                { it -> (it as CodeList).codeListData() }
            )

        assertThat(markers).containsExactly(
            "fixed_codelist_0",
            "fixed_codelist_1"
        )
    }

    @Test
    fun `Should have codepages @ root # taxonomyunit # codelist`() {
        val codesPages = folderSourceBundle.taxonomyUnits()[0].codeLists()[0].codePagesData().asSequence().toList()
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
