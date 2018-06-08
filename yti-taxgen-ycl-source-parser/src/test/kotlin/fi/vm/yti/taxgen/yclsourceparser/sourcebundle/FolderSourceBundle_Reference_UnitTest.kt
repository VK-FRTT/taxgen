package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Paths


@DisplayName("when folder source bundle reads reference folder structure")
internal class FolderSourceBundle_Reference_UnitTest : SourceBundle_UnitTestBase() {

    private lateinit var bundle: FolderSourceBundle

    @BeforeEach
    fun init() {
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceUri =
            classLoader.getResource("foldersourcebundle_reference_unittest").toURI()
        val resourcePath = Paths.get(resourceUri)
        bundle = FolderSourceBundle(resourcePath)
    }

    @AfterEach
    fun teardown() {
        bundle.close()
    }

    @Test
    fun `Should have bundleinfo @ root`() {
        val infoJson = objectMapper.readTree(bundle.bundleInfoData())

        assertThat(infoJson.isObject).isTrue()
        assertThat(infoJson.get("marker") as Any).isNotNull()
        assertThat(infoJson.get("marker").textValue()).isEqualTo("fsb_reference_content")
    }

    @Test
    fun `Should have taxonomyunits @ root # taxonomyunit`() {
        val taxonomyUnits = bundle.taxonomyUnits()
        val markers =
            extractMarkerValuesFromJsonData(
                taxonomyUnits,
                { it -> (it as TaxonomyUnit).taxonomyUnitInfoData() }
            )

        assertThat(markers).containsExactly(
            "taxonomyunit_0",
            "taxonomyunit_1",
            "taxonomyunit_2",
            "taxonomyunit_3",
            "taxonomyunit_4",
            "taxonomyunit_5",
            "taxonomyunit_6",
            "taxonomyunit_7",
            "taxonomyunit_8",
            "taxonomyunit_9",
            "taxonomyunit_10",
            "taxonomyunit_11"
        )
    }

    @Test
    fun `Should have codelists @ root # taxonomyunit # codelist`() {
        val codeLists = bundle.taxonomyUnits()[0].codeLists()
        val markers =
            extractMarkerValuesFromJsonData(
                codeLists,
                { it -> (it as CodeList).codeListData() }
            )

        assertThat(markers).containsExactly(
            "codelist_0",
            "codelist_1",
            "codelist_2",
            "codelist_3",
            "codelist_4",
            "codelist_5",
            "codelist_6",
            "codelist_7",
            "codelist_8",
            "codelist_9",
            "codelist_10",
            "codelist_11"
        )
    }

    @Test
    fun `Should have codepages @ root # taxonomyunit # codelist`() {
        val codesPages = bundle.taxonomyUnits()[0].codeLists()[0].codePagesData().asSequence().toList()
        val markers =
            extractMarkerValuesFromJsonData(
                codesPages,
                { it -> it as String }
            )

        assertThat(markers).containsExactly(
            "codepage_0",
            "codepage_1",
            "codepage_2",
            "codepage_3",
            "codepage_4",
            "codepage_5",
            "codepage_6",
            "codepage_7",
            "codepage_8",
            "codepage_9",
            "codepage_10",
            "codepage_11"
        )
    }
}
