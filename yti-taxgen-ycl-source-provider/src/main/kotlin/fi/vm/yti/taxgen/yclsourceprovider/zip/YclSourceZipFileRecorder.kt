package fi.vm.yti.taxgen.yclsourceprovider.zip

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSourceRecorder
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureRecorder
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

class YclSourceZipFileRecorder(
    targetZipPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : YclSourceRecorder {

    private val targetZipPath = targetZipPath.toAbsolutePath().normalize()
    private var zipFileSystem: FileSystem? = null
    private var folderStructureRecorder: YclSourceRecorder? = null

    override fun contextName(): String = "ZIP file"
    override fun contextRef(): String = targetZipPath.toString()

    override fun captureSources(yclSource: YclSource) {
        diagnostic.withContext(this) {
            val fs = createTargetZipFileSystem().also { zipFileSystem = it }
            val baseFolderPath = fs.getPath("/")
            val recorder = createFolderStructureRecorder(baseFolderPath).also { folderStructureRecorder = it }

            recorder.captureSources(yclSource)
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

    private fun createFolderStructureRecorder(baseFolderPath: Path): YclSourceRecorder {
        return YclSourceFolderStructureRecorder(
            baseFolderPath = baseFolderPath,
            forceOverwrite = false,
            diagnostic = diagnostic
        )
    }
}
