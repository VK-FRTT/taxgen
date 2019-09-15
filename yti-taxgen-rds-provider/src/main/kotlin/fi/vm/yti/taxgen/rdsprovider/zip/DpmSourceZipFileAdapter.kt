package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfigHolder
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

internal class DpmSourceZipFileAdapter(
    sourceZipPath: Path,
    private val diagnosticContext: DiagnosticContext
) : DpmSource {

    private val sourceZipPath = sourceZipPath.toAbsolutePath().normalize()
    private val zipFileSystem = createSourceZipFileSystem()
    private val folderAdapter = createDpmSourceFolderAdapter()

    override fun contextLabel(): String = "ZIP file"
    override fun contextIdentifier(): String = sourceZipPath.toString()

    override fun config(): DpmSourceConfigHolder = folderAdapter.config()

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        folderAdapter.eachDpmDictionarySource(action)
    }

    fun close() {
        zipFileSystem.close()
    }

    private fun createSourceZipFileSystem(): FileSystem {
        return FileSystems.newFileSystem(
            sourceZipUri(),
            emptyMap<String, String>()
        )
    }

    private fun sourceZipUri() = URI.create("jar:file:$sourceZipPath")

    private fun createDpmSourceFolderAdapter(): DpmSource {
        return DpmSourceFolderAdapter(
            dpmSourceRootPath = rootPathWithinZip(),
            diagnosticContext = diagnosticContext
        )
    }

    private fun rootPathWithinZip() = zipFileSystem.getPath("/")
}
