package fi.vm.yti.taxgen.yclsourceprovider.zip

import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

class YclSourceZipFileAdapter(
    sourceZipPath: Path
) : YclSource {

    private val sourceZipPath = sourceZipPath.toAbsolutePath().normalize()
    private val zipFileSystem = createSourceZipFileSystem()
    private val folderStructureAdapter = createFolderStructureAdapter()

    override fun sourceInfoData(): String = folderStructureAdapter.sourceInfoData()

    override fun dpmDictionarySources(): List<DpmDictionarySource> = folderStructureAdapter.dpmDictionarySources()

    override fun close() {
        folderStructureAdapter.close()
        zipFileSystem.close()
    }

    private fun createSourceZipFileSystem(): FileSystem {
        return FileSystems.newFileSystem(
            sourceZipUri(),
            emptyMap<String, String>()
        )
    }

    private fun sourceZipUri() = URI.create("jar:file:$sourceZipPath")

    private fun createFolderStructureAdapter(): YclSource {
        return YclSourceFolderStructureAdapter(
            baseFolderPath = rootPathWithinZip()
        )
    }

    private fun rootPathWithinZip() = zipFileSystem.getPath("/")
}
