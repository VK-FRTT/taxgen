package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.config.input.DpmSourceConfigInput
import fi.vm.yti.taxgen.rdsprovider.contextdiagnostic.DpmSourceRecorderContextDecorator
import fi.vm.yti.taxgen.rdsprovider.contextdiagnostic.SourceProviderContextDecorator
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.SourceProviderFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.rds.SourceProviderRdsAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceRecorderZipFileAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.SourceProviderZipFileAdapter
import java.nio.file.Path

object ProviderFactory {

    fun providerForConfigFile(
        configFilePath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceProvider {
        val sourceProvider = resolveSourceProviderForConfigFile(
            configFilePath,
            diagnosticContext
        )

        return SourceProviderContextDecorator(
            sourceProvider = sourceProvider,
            diagnosticContext = diagnosticContext
        )
    }

    private fun resolveSourceProviderForConfigFile(
        configFilePath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceProvider {
        return diagnosticContext.withContext(
            contextType = DiagnosticContextType.InitConfiguration,
            contextIdentifier = configFilePath.fileName.toString()
        ) {
            val dpmSourceConfig = DpmSourceConfigInput.tryReadAndValidateConfig(
                configFilePath,
                diagnosticContext
            ) ?: diagnosticContext.fatal("Unsupported config file: $configFilePath")

            SourceProviderRdsAdapter(
                dpmSourceConfig = dpmSourceConfig,
                diagnostic = diagnosticContext
            )
        }
    }

    fun folderProvider(
        sourceRootPath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceProvider {
        val sourceProvider = SourceProviderFolderAdapter(
            dpmSourceRootPath = sourceRootPath
        )

        return SourceProviderContextDecorator(
            sourceProvider = sourceProvider,
            diagnosticContext = diagnosticContext
        )
    }

    fun zipFileProvider(
        zipFilePath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceProvider {
        val sourceProvider = SourceProviderZipFileAdapter(
            sourceZipPath = zipFilePath
        )

        return SourceProviderContextDecorator(
            sourceProvider = sourceProvider,
            diagnosticContext = diagnosticContext
        )
    }

    fun folderRecorder(
        baseFolderPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmSourceRecorder {
        val dpmSourceRecorder = DpmSourceRecorderFolderAdapter(
            baseFolderPath = baseFolderPath,
            forceOverwrite = forceOverwrite,
            diagnostic = diagnosticContext
        )

        return DpmSourceRecorderContextDecorator(
            dpmSourceRecorder = dpmSourceRecorder,
            diagnosticContext = diagnosticContext
        )
    }

    fun zipRecorder(
        zipFilePath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmSourceRecorder {
        val dpmSourceRecorder = DpmSourceRecorderZipFileAdapter(
            targetZipPath = zipFilePath,
            forceOverwrite = forceOverwrite,
            diagnostic = diagnosticContext
        )

        return DpmSourceRecorderContextDecorator(
            dpmSourceRecorder = dpmSourceRecorder,
            diagnosticContext = diagnosticContext
        )
    }
}
