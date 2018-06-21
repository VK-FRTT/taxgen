package fi.vm.yti.taxgen.yclsourceprovider.zip

import fi.vm.yti.taxgen.yclsourceprovider.SourceBundle
import fi.vm.yti.taxgen.yclsourceprovider.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceprovider.folder.FolderSourceBundleWriter
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class ZipSourceBundleWriter(
    targetZipPath: Path,
    private val sourceBundle: SourceBundle,
    private val forceOverwrite: Boolean
) : SourceBundleWriter {

    private val targetZipPath = targetZipPath.toAbsolutePath().normalize()
    private val zipFileSystem = createTargetZipFileSystem()
    private val bundleWriter = createBundleWriter()

    override fun write() {
        bundleWriter.write()
    }

    override fun close() {
        zipFileSystem.close()
        bundleWriter.close()
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

    private fun createBundleWriter(): SourceBundleWriter {
        return FolderSourceBundleWriter(
            baseFolderPath = rootPathWithinZip(),
            sourceBundle = sourceBundle,
            forceOverwrite = false
        )
    }

    private fun rootPathWithinZip() = zipFileSystem.getPath("/")
}
