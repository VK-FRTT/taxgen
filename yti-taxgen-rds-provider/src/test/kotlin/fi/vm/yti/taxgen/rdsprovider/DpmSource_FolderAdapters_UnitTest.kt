package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import java.nio.file.Path
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DpmSource_FolderAdapters_UnitTest : DpmSource_UnitTestBase() {

    private var loopbackTempFolder: TempFolder? = null

    @AfterAll
    fun teardown() {
        loopbackTempFolder.apply { this?.close() }
        loopbackTempFolder = null
    }

    private fun dpmSourceFolderAdapterToReferenceData(): Pair<DpmSourceFolderAdapter, Path> {
        val classLoader = Thread.currentThread().contextClassLoader
        val referenceUri = classLoader.getResource("folder_adapter_reference").toURI()
        val dpmSourceRootPath = Paths.get(referenceUri)
        return Pair(DpmSourceFolderAdapter(dpmSourceRootPath), dpmSourceRootPath)
    }

    @TestFactory
    fun `Test RDS source folder adapter against static reference folder structure`(): List<DynamicNode> {
        val (source, rootPath) = dpmSourceFolderAdapterToReferenceData()
        return testCaseFactory(source, rootPath)
    }

    @TestFactory
    fun `Test RDS source folder adapter against loopback folder structure`(): List<DynamicNode> {
        val tempFolder = TempFolder("rds_source_folder_loopback")
        loopbackTempFolder = tempFolder

        DpmSourceRecorderFolderAdapter(
            baseFolderPath = tempFolder.path(),
            forceOverwrite = false,
            diagnostic = diagnostic
        ).use {
            val (source, _) = dpmSourceFolderAdapterToReferenceData()
            it.captureSources(source)
        }

        val dpmSource = DpmSourceFolderAdapter(tempFolder.path())
        return testCaseFactory(dpmSource, tempFolder.path())
    }

    fun testCaseFactory(dpmSource: DpmSource, dpmSourceRootPath: Path): List<DynamicNode> {
        return listOf(
            dynamicContainer(
                "DpmSourceRoot",
                listOf(
                    dynamicTest("Should have diagnostic context info about RDS source") {
                        assertThat(dpmSource.contextType()).isEqualTo(DiagnosticContextType.DpmSource)
                        assertThat(dpmSource.contextLabel()).isEqualTo("folder")
                        assertThat(dpmSource.contextIdentifier()).isEqualTo(dpmSourceRootPath.toString())

                    },

                    dynamicTest("Should have source config") {
                        val marker = extractMarkerValueFromJsonData {
                            dpmSource.sourceConfigData()
                        }

                        assertThat(marker).isEqualTo("folder_adapter_reference/meta/source_config")
                    }
                )
            ),

            dynamicContainer(
                "DpmDictionaryUnit",
                listOf()
            )
        )
    }
}
