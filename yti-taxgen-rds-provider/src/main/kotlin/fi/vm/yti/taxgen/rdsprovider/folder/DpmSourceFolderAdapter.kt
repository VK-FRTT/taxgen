package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.helpers.SortOps
import java.nio.file.Path

class DpmSourceFolderAdapter(
    baseFolderPath: Path
) : DpmSource() {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun contextLabel(): String = "folder"
    override fun contextIdentifier(): String = baseFolderPath.toString()

    override fun sourceConfigData(): String {
        return FileOps.readTextFile(baseFolderPath.resolve("meta"), "source_config.json")
    }

    override fun dpmDictionarySources(): Sequence<DpmDictionarySource> {
        val paths = FileOps.listSubFoldersMatching(baseFolderPath, "dpm_dictionary_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)
        return sortedPaths.map { path -> DpmDictionarySourceFolderAdapter(path) }.asSequence()
    }

    override fun close() {}
}
