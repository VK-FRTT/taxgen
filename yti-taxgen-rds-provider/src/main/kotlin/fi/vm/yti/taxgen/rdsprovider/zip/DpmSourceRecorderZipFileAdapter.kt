package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

internal class DpmSourceRecorderZipFileAdapter(
    targetZipPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : DpmSourceRecorder {

    private val targetZipPath = targetZipPath.toAbsolutePath().normalize()
    private var zipFileSystem: FileSystem? = null
    private var folderStructureRecorder: DpmSourceRecorder? = null

    override fun contextLabel(): String = "ZIP file"
    override fun contextIdentifier(): String = targetZipPath.toString()

    override fun captureSources(dpmSource: DpmSource) {
        diagnostic.withContext(this) {
            val fs = createTargetZipFileSystem().also { zipFileSystem = it }
            val baseFolderPath = fs.getPath("/")
            val recorder = createFolderStructureRecorder(baseFolderPath).also { folderStructureRecorder = it }

            recorder.captureSources(dpmSource)
        }
    }

    override fun close() {
        folderStructureRecorder?.close()
        zipFileSystem?.close()
    }

    private fun createTargetZipFileSystem(): FileSystem {
        FileOps.deleteConflictingTargetFileIfAllowed(targetZipPath, forceOverwrite)
        FileOps.failIfTargetFileExists(targetZipPath, diagnostic)
        FileOps.createIntermediateFolders(targetZipPath)

        return FileSystems.newFileSystem(
            targetZipUri(),
            zipOptions()
        )
    }

    private fun targetZipUri() = URI.create("jar:file:$targetZipPath")

    private fun zipOptions() = mapOf("create" to "true")

    private fun createFolderStructureRecorder(baseFolderPath: Path): DpmSourceRecorder {
        return DpmSourceRecorderFolderAdapter(
            baseFolderPath = baseFolderPath,
            forceOverwrite = false,
            diagnostic = diagnostic
        )
    }
}
