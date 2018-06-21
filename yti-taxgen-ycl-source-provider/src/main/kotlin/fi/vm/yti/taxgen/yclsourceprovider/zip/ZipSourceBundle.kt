package fi.vm.yti.taxgen.yclsourceprovider.zip

import fi.vm.yti.taxgen.yclsourceprovider.SourceBundle
import fi.vm.yti.taxgen.yclsourceprovider.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceprovider.folder.FolderSourceBundle
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

class ZipSourceBundle(
    sourceZipPath: Path
) : SourceBundle {

    private val sourceZipPath = sourceZipPath.toAbsolutePath().normalize()
    private val zipFileSystem = createSourceZipFileSystem()
    private val sourceBundle = createSourceBundle()

    override fun bundleInfoData(): String = sourceBundle.bundleInfoData()

    override fun taxonomyUnits(): List<TaxonomyUnit> = sourceBundle.taxonomyUnits()

    override fun close() {
        zipFileSystem.close()
        sourceBundle.close()
    }

    private fun createSourceZipFileSystem(): FileSystem {
        return FileSystems.newFileSystem(
            sourceZipUri(),
            emptyMap<String, String>()
        )
    }

    private fun sourceZipUri() = URI.create("jar:file:$sourceZipPath")

    private fun createSourceBundle(): SourceBundle {
        return FolderSourceBundle(
            baseFolderPath = rootPathWithinZip()
        )
    }

    private fun rootPathWithinZip() = zipFileSystem.getPath("/")
}
