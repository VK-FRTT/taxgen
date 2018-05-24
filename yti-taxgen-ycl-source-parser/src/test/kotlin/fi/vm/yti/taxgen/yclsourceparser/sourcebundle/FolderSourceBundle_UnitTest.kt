package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class FolderSourceBundle_UnitTest {

    @Nested
    @DisplayName("when bundle contents are read from reference folder content")
    inner class ReadFromReference {

        private val objectMapper = jacksonObjectMapper()

        private lateinit var bundle: FolderSourceBundle

        @BeforeEach
        fun init() {
            val resourceUri = this::class.java.getResource("fsb_reference_content").toURI()
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
            assertThat(taxonomyUnits.size).isEqualTo(12)

            val markers = taxonomyUnits.map {
                val infoJson = objectMapper.readTree(it.taxonomyUnitInfoData())
                assertThat(infoJson.isObject).isTrue()

                infoJson.get("marker").textValue()
            }

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
            assertThat(codeLists.size).isEqualTo(12)

            val markers = codeLists.map {
                val infoJson = objectMapper.readTree(it.codeListData())
                assertThat(infoJson.isObject).isTrue()

                infoJson.get("marker").textValue()
            }

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
            assertThat(codesPages.size).isEqualTo(12)

            val markers = codesPages.map {
                val infoJson = objectMapper.readTree(it)
                assertThat(infoJson.isObject).isTrue()

                infoJson.get("marker").textValue()
            }

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
}
