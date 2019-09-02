package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
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
    private var folderStructureRecorder: DpmSourceRecorder? = null

    override fun contextLabel(): String = "ZIP file"
    override fun contextIdentifier(): String = outputZipPath.toString()

    override fun captureSources(sourceProvider: SourceProvider) {
        val fs = createTargetZipFileSystem().also { outputZipFileSystem = it }
        val baseFolderPath = fs.getPath("/")
        val recorder = createFolderStructureRecorder(baseFolderPath).also { folderStructureRecorder = it }

        recorder.captureSources(sourceProvider)
    }

    override fun close() {
        folderStructureRecorder?.close()
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

    private fun createFolderStructureRecorder(baseFolderPath: Path): DpmSourceRecorder {
        return DpmSourceRecorderFolderAdapter(
            outputFolderPath = baseFolderPath,
            forceOverwrite = false,
            diagnostic = diagnostic
        )
    }
}
