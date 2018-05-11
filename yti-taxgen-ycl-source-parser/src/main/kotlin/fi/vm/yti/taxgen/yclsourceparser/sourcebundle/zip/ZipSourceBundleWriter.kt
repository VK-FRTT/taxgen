package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.zip

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundleWriter
import java.io.Closeable
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class ZipSourceBundleWriter(
    private val targetZipPath: Path,
    private val sourceBundle: SourceBundle,
    private val forceOverwrite: Boolean
) : SourceBundleWriter {

    var zipFsResource: Closeable? = null
    var bundleWriterResource: Closeable? = null

    override fun write() {
        deleteTargetFileIfAllowed()

        val zipFs = createTargetZipFileSystem()
        val rootPath = rootPathWithinZip(zipFs)
        val bundleWriter = createBundleWriter(rootPath)

        bundleWriter.write()
    }

    override fun close() {
        zipFsResource?.close()
        zipFsResource = null

        bundleWriterResource?.close()
        bundleWriterResource = null
    }

    private fun deleteTargetFileIfAllowed() {
        if (forceOverwrite) Files.deleteIfExists(targetZipPath)
    }

    private fun createTargetZipFileSystem(): FileSystem {
        return FileSystems.newFileSystem(
            targetZipUri(),
            zipOptions()
        ).also { zipFsResource = it }
    }

    private fun targetZipUri() = URI.create("jar:file:$targetZipPath")

    private fun zipOptions() = mapOf("create" to "true")

    private fun rootPathWithinZip(zipFs: FileSystem) = zipFs.getPath("/")

    private fun createBundleWriter(folderPath: Path): FolderSourceBundleWriter {
        return FolderSourceBundleWriter(
            baseFolderPath = folderPath,
            sourceBundle = sourceBundle,
            forceOverwrite = false
        ).also { bundleWriterResource = it }
    }
}
