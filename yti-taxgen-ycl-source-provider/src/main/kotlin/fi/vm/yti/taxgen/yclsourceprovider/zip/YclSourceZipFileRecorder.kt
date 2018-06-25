package fi.vm.yti.taxgen.yclsourceprovider.zip

import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSourceRecorder
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureRecorder
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class YclSourceZipFileRecorder(
    targetZipPath: Path,
    private val yclSource: YclSource,
    private val forceOverwrite: Boolean
) : YclSourceRecorder {

    private val targetZipPath = targetZipPath.toAbsolutePath().normalize()
    private val zipFileSystem = createTargetZipFileSystem()
    private val folderStructureRecorder = createFolderStructureRecorder()

    override fun capture() {
        folderStructureRecorder.capture()
    }

    override fun close() {
        folderStructureRecorder.close()
        zipFileSystem.close()
    }

    private fun createTargetZipFileSystem(): FileSystem {
        deleteTargetFileIfAllowed()
        ensureTargetFoldersExist()

        return FileSystems.newFileSystem(
            targetZipUri(),
            zipOptions()
        )
    }

    private fun deleteTargetFileIfAllowed() {
        if (forceOverwrite) Files.deleteIfExists(targetZipPath)
    }

    private fun ensureTargetFoldersExist() = Files.createDirectories(targetZipPath.parent)

    private fun targetZipUri() = URI.create("jar:file:$targetZipPath")

    private fun zipOptions() = mapOf("create" to "true")

    private fun createFolderStructureRecorder(): YclSourceRecorder {
        return YclSourceFolderStructureRecorder(
            baseFolderPath = rootPathWithinZip(),
            yclSource = yclSource,
            forceOverwrite = false
        )
    }

    private fun rootPathWithinZip() = zipFileSystem.getPath("/")
}
