package fi.vm.yti.taxgen.rdsource

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.configdata.ConfigFactory
import fi.vm.yti.taxgen.rdsource.contextdiagnostic.DpmSourceRecorderContextDecorator
import fi.vm.yti.taxgen.rdsource.contextdiagnostic.SourceHolderContextDecorator
import fi.vm.yti.taxgen.rdsource.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.rdsource.folder.SourceHolderFolderAdapter
import fi.vm.yti.taxgen.rdsource.rds.SourceHolderRdsAdapter
import fi.vm.yti.taxgen.rdsource.zip.DpmSourceRecorderZipFileAdapter
import fi.vm.yti.taxgen.rdsource.zip.SourceHolderZipFileAdapter
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
        val configHolder = ConfigFactory.dpmSourceConfigFromFile(
            configFilePath,
            diagnosticContext
        )

        return SourceHolderRdsAdapter(
            configHolder = configHolder,
            diagnosticContext = diagnosticContext
        )
    }

    fun sourceForFolder(
        sourceRootPath: Path,
        diagnosticContext: DiagnosticContext
    ): SourceHolder {
        val sourceHolder = SourceHolderFolderAdapter(
            dpmSourceRootPath = sourceRootPath,
            diagnosticContext = diagnosticContext
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
            sourceZipPath = zipFilePath,
            diagnosticContext = diagnosticContext
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
