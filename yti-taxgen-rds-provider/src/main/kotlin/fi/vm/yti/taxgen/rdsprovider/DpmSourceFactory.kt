package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.diagnostic.DpmSourceContextDecorator
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.rds.DpmSourceRdsAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceRecorderZipFileAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceZipFileAdapter
import java.nio.file.Path

object DpmSourceFactory {

    fun rdsSource(configFilePath: Path, diagnostic: Diagnostic): DpmSource {
        val adapter = DpmSourceRdsAdapter(
            configPath = configFilePath,
            diagnostic = diagnostic
        )

        return DpmSourceContextDecorator(
            dpmSource = adapter,
            diagnostic = diagnostic
        )
    }

    fun folderSource(sourceRootPath: Path, diagnostic: Diagnostic): DpmSource {
        val adapter = DpmSourceFolderAdapter(
            dpmSourceRootPath = sourceRootPath
        )

        return DpmSourceContextDecorator(
            dpmSource = adapter,
            diagnostic = diagnostic
        )
    }

    fun zipFileSource(zipFilePath: Path, diagnostic: Diagnostic): DpmSource {
        val adapter = DpmSourceZipFileAdapter(
            sourceZipPath = zipFilePath
        )

        return DpmSourceContextDecorator(
            dpmSource = adapter,
            diagnostic = diagnostic
        )
    }

    fun folderRecorder(baseFolderPath: Path, forceOverwrite: Boolean, diagnostic: Diagnostic): DpmSourceRecorder {
        return DpmSourceRecorderFolderAdapter(
            baseFolderPath = baseFolderPath,
            forceOverwrite = forceOverwrite,
            diagnostic = diagnostic
        )
    }

    fun zipRecorder(zipFilePath: Path, forceOverwrite: Boolean, diagnostic: Diagnostic): DpmSourceRecorder {
        return DpmSourceRecorderZipFileAdapter(
            targetZipPath = zipFilePath,
            forceOverwrite = forceOverwrite,
            diagnostic = diagnostic
        )
    }
}
