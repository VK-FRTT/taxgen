package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

internal class DpmSourceZipFileAdapter(
    sourceZipPath: Path
) : DpmSource {

    private val sourceZipPath = sourceZipPath.toAbsolutePath().normalize()
    private val zipFileSystem = createSourceZipFileSystem()
    private val folderStructureAdapter = createFolderStructureAdapter()

    override fun contextLabel(): String = "ZIP file"
    override fun contextIdentifier(): String = sourceZipPath.toString()

    override fun sourceConfigData(): String = folderStructureAdapter.sourceConfigData()

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        folderStructureAdapter.eachDpmDictionarySource(action)
    }

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

    private fun createFolderStructureAdapter(): DpmSource {
        return DpmSourceFolderAdapter(
            dpmSourceRootPath = rootPathWithinZip()
        )
    }

    private fun rootPathWithinZip() = zipFileSystem.getPath("/")
}
