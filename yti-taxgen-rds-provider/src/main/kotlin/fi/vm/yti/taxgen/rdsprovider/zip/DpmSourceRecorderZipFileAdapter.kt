package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

internal class DpmSourceRecorderZipFileAdapter(
    outputZipPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : DpmSourceRecorder {

    private val outputZipPath = outputZipPath.toAbsolutePath().normalize()
    private var outputZipFileSystem: FileSystem? = null
    private var folderRecorder: DpmSourceRecorder? = null

    override fun contextTitle(): String = "ZIP file"
    override fun contextIdentifier(): String = outputZipPath.toString()

    override fun captureSources(dpmSource: DpmSource) {
        val fs = createTargetZipFileSystem().also { outputZipFileSystem = it }
        val baseFolderPath = fs.getPath("/")
        val recorder = createDpmSourceRecorderFolderAdapter(baseFolderPath).also { folderRecorder = it }

        recorder.captureSources(dpmSource)
    }

    override fun close() {
        folderRecorder?.close()
        outputZipFileSystem?.close()
    }

    private fun createTargetZipFileSystem(): FileSystem {
        FileOps.deleteConflictingOutputFileIfAllowed(outputZipPath, forceOverwrite)
        FileOps.failIfOutputFileExists(outputZipPath, diagnostic)
        FileOps.createIntermediateFolders(outputZipPath)

        return FileSystems.newFileSystem(
            outputZipUri(),
            zipOptions()
        )
    }

    private fun outputZipUri() = URI.create("jar:file:$outputZipPath")

    private fun zipOptions() = mapOf("create" to "true")

    private fun createDpmSourceRecorderFolderAdapter(baseFolderPath: Path): DpmSourceRecorder {
        return DpmSourceRecorderFolderAdapter(
            outputFolderPath = baseFolderPath,
            forceOverwrite = false,
            diagnostic = diagnostic
        )
    }
}
