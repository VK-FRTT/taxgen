package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.config.input.DpmSourceConfigInput
import fi.vm.yti.taxgen.rdsprovider.contextdiagnostic.DpmSourceRecorderContextDecorator
import fi.vm.yti.taxgen.rdsprovider.contextdiagnostic.SourceHolderContextDecorator
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.SourceHolderFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.rds.SourceHolderRdsAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceRecorderZipFileAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.SourceHolderZipFileAdapter
import java.nio.file.Path

object SourceFactory {

    fun sourceForConfigFile(
        configFilePath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceHolder {
        val sourceHolder = resolveSourceHolderForConfigFile(
            configFilePath,
            diagnosticContext
        )

        return SourceHolderContextDecorator(
            realSourceHolder = sourceHolder,
            diagnosticContext = diagnosticContext
        )
    }

    private fun resolveSourceHolderForConfigFile(
        configFilePath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceHolder {
        return diagnosticContext.withContext(
            contextType = DiagnosticContextType.InitConfiguration,
            contextIdentifier = configFilePath.fileName.toString()
        ) {
            val dpmSourceConfig = DpmSourceConfigInput.tryReadAndValidateConfig(
                configFilePath,
                diagnosticContext
            ) ?: diagnosticContext.fatal("Unsupported config file: $configFilePath")

            SourceHolderRdsAdapter(
                dpmSourceConfig = dpmSourceConfig,
                diagnostic = diagnosticContext
            )
        }
    }

    fun sourceForFolder(
        sourceRootPath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceHolder {
        val sourceHolder = SourceHolderFolderAdapter(
            dpmSourceRootPath = sourceRootPath
        )

        return SourceHolderContextDecorator(
            realSourceHolder = sourceHolder,
            diagnosticContext = diagnosticContext
        )
    }

    fun sourceForZipFile(
        zipFilePath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceHolder {
        val sourceHolder = SourceHolderZipFileAdapter(
            sourceZipPath = zipFilePath
        )

        return SourceHolderContextDecorator(
            realSourceHolder = sourceHolder,
            diagnosticContext = diagnosticContext
        )
    }

    fun folderRecorder(
        outputFolderPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmSourceRecorder {
        val dpmSourceRecorder = DpmSourceRecorderFolderAdapter(
            outputFolderPath = outputFolderPath,
            forceOverwrite = forceOverwrite,
            diagnostic = diagnosticContext
        )

        return DpmSourceRecorderContextDecorator(
            realDpmSourceRecorder = dpmSourceRecorder,
            diagnosticContext = diagnosticContext
        )
    }

    fun zipRecorder(
        outputZipPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmSourceRecorder {
        val dpmSourceRecorder = DpmSourceRecorderZipFileAdapter(
            outputZipPath = outputZipPath,
            forceOverwrite = forceOverwrite,
            diagnostic = diagnosticContext
        )

        return DpmSourceRecorderContextDecorator(
            realDpmSourceRecorder = dpmSourceRecorder,
            diagnosticContext = diagnosticContext
        )
    }
}
