package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.zipwriter

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folderwriter.FolderSourceBundleWriter
import java.io.Closeable
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path


class ZipSourceBundleWriter(
    private val rootPath: Path,
    private val sourceBundle: SourceBundle
) : SourceBundleWriter {

    var zipFsResource: Closeable? = null
    var bundleWriterResource: Closeable? = null

    override fun write() {
        val zipFileSystem = FileSystems.newFileSystem(
            targetZipUri(),
            targetZipOptions()
        ).also { zipFsResource = it }


        val folderSourceBundleWriter = FolderSourceBundleWriter(
            rootPath = zipFileSystem.getPath("/"),
            sourceBundle = sourceBundle
        ).also { bundleWriterResource = it }

        folderSourceBundleWriter.write()
    }


    override fun close() {
        zipFsResource?.close()
        zipFsResource = null

        bundleWriterResource?.close()
        bundleWriterResource = null
    }

    private fun targetZipUri() = URI.create("jar:file:$rootPath")

    private fun targetZipOptions() = mapOf("create" to "true")
}
