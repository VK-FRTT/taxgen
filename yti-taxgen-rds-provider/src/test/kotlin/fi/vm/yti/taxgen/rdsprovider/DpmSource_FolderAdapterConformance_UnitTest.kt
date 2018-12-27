package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceRecorderZipFileAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceZipFileAdapter
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory

@DisplayName("Test RDS source adapter conformance")
internal class DpmSource_FolderAdapterConformance_UnitTest : DpmSource_ConformanceUnitTestBase() {

    companion object {
        lateinit var loopbackTempFolder: TempFolder
        lateinit var zipLoopbackTempFolder: TempFolder

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            loopbackTempFolder = TempFolder("conformance_loopback")
            zipLoopbackTempFolder = TempFolder("conformance_zip_loopback")
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            loopbackTempFolder.close()
            zipLoopbackTempFolder.close()
        }
    }

    @TestFactory
    fun `Folder adapter with static reference data`(): List<DynamicNode> {
        val (source, rootPath) = dpmSourceFolderAdapterToReferenceData()

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = rootPath.toString()
        )

        return createAdapterConformanceTestCases(source, expectedDetails)
    }

    @TestFactory
    fun `Folder adapter with loopback data`(): List<DynamicNode> {
        DpmSourceRecorderFolderAdapter(
            baseFolderPath = loopbackTempFolder.path(),
            forceOverwrite = false,
            diagnostic = diagnostic
        ).use {
            val (source, _) = dpmSourceFolderAdapterToReferenceData()
            it.captureSources(source)
        }

        val dpmSource = DpmSourceFolderAdapter(loopbackTempFolder.path())

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = loopbackTempFolder.path().toString()
        )

        return createAdapterConformanceTestCases(dpmSource, expectedDetails)
    }

    @TestFactory
    fun `Folder adapter with zip-loopback data`(): List<DynamicNode> {
        val targetZipPath = zipLoopbackTempFolder.resolve("file.zip")

        DpmSourceRecorderZipFileAdapter(
            targetZipPath = targetZipPath,
            forceOverwrite = false,
            diagnostic = diagnostic
        ).use {
            val (source, _) = dpmSourceFolderAdapterToReferenceData()
            it.captureSources(source)
        }

        val dpmSource = DpmSourceZipFileAdapter(targetZipPath)

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "ZIP file",
            dpmSourceContextIdentifier = targetZipPath.toString()
        )

        return createAdapterConformanceTestCases(dpmSource, expectedDetails)
    }
}
