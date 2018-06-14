package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.zip.ZipSourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.zip.ZipSourceBundleWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

@DisplayName("When bundle contents are written to zip file and then read from the zip")
internal class ZipSourceBundle_Loopback_UnitTest : SourceBundle_UnitTestBase() {

    private lateinit var targetFolderPath: Path
    private lateinit var zipSourceBundle: ZipSourceBundle

    @BeforeEach
    fun init() {
        targetFolderPath = Files.createTempDirectory("zipsourcebundle_loopback_unittest")
        val targetZipPath = targetFolderPath.resolve("loopback.zip")

        val sourceBundle = FixedSourceBundle()

        ZipSourceBundleWriter(
            targetZipPath = targetZipPath,
            sourceBundle = sourceBundle,
            forceOverwrite = false
        ).use {
            it.write()
        }

        zipSourceBundle = ZipSourceBundle(targetZipPath)
    }

    @AfterEach
    fun teardown() {
        zipSourceBundle.close()

        Files
            .walk(targetFolderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun `Should have bundleinfo @ root`() {
        val infoJson = objectMapper.readTree(zipSourceBundle.bundleInfoData())

        assertThat(infoJson.isObject).isTrue()
        assertThat(infoJson.get("marker") as Any).isNotNull()
        assertThat(infoJson.get("marker").textValue()).isEqualTo("fixed_source_bundle")
    }

    @Test
    fun `Should have taxonomyunits @ root # taxonomyunit`() {
        val taxonomyUnits = zipSourceBundle.taxonomyUnits()
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
        val codeLists = zipSourceBundle.taxonomyUnits()[0].codeLists()
        val markers = extractMarkerValuesFromJsonData(
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
        val codesPages = zipSourceBundle.taxonomyUnits()[0].codeLists()[0].codePagesData().asSequence().toList()
        val markers = extractMarkerValuesFromJsonData(
            codesPages,
            { it -> it as String }
        )

        assertThat(markers).containsExactly(
            "fixed_codepage_0",
            "fixed_codepage_1"
        )
    }
}
