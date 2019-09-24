package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory

internal class DpmSource_FunctionalConformance_FolderAdapter_ModuleTest : DpmSource_FunctionalConformance_ModuleTestBase() {

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
        val (sourceHolder, rootPath) = sourceHolderFolderAdapterForBundledReferenceData(
            diagnosticContext,
            false
        )

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = rootPath.toString(),
            dpmSourceConfigFilePath = "$rootPath/meta/source_config.json"
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }

    @TestFactory
    fun `Context decorated folder adapter with static reference data`(): List<DynamicNode> {
        val (sourceHolder, rootPath) = sourceHolderFolderAdapterForBundledReferenceData(
            diagnosticContext,
            true
        )

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = rootPath.toString(),
            dpmSourceConfigFilePath = "$rootPath/meta/source_config.json"
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }

    @TestFactory
    fun `Folder adapter with loopback data`(): List<DynamicNode> {
        SourceFactory.folderRecorder(
            outputFolderPath = loopbackTempFolder.path(),
            forceOverwrite = false,
            diagnosticContext = diagnosticContext
        ).use { sourceRecorder ->
            val (sourceHolder, _) = sourceHolderFolderAdapterForBundledReferenceData(
                diagnosticContext = diagnosticContext,
                contextDecorateSource = false
            )
            sourceHolder.withDpmSource {
                sourceRecorder.captureSources(it)
            }
        }

        val sourceHolder = SourceFactory.sourceForFolder(
            sourceRootPath = loopbackTempFolder.path(),
            diagnosticContext = diagnosticContext
        )

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = loopbackTempFolder.path().toString(),
            dpmSourceConfigFilePath = "${loopbackTempFolder.path()}/meta/source_config.json"
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }

    @TestFactory
    fun `Folder adapter with zip-loopback data`(): List<DynamicNode> {
        val targetZipPath = zipLoopbackTempFolder.resolve("file.zip")

        SourceFactory.zipRecorder(
            outputZipPath = targetZipPath,
            forceOverwrite = false,
            diagnosticContext = diagnosticContext
        ).use { sourceRecorder ->
            val (sourceHolder, _) = sourceHolderFolderAdapterForBundledReferenceData(
                diagnosticContext = diagnosticContext,
                contextDecorateSource = false
            )

            sourceHolder.withDpmSource {
                sourceRecorder.captureSources(it)
            }
        }

        val sourceHolder = SourceFactory.sourceForZipFile(
            zipFilePath = targetZipPath,
            diagnosticContext = diagnosticContext
        )

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "ZIP file",
            dpmSourceContextIdentifier = targetZipPath.toString(),
            dpmSourceConfigFilePath = "/meta/source_config.json"
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }
}
