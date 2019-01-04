package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.helpers.SortOps
import java.nio.file.Path

internal class DpmSourceFolderAdapter(
    dpmSourceRootPath: Path
) : DpmSource {

    private val dpmSourceRootPath = dpmSourceRootPath.toAbsolutePath().normalize()

    override fun contextLabel(): String = "folder"
    override fun contextIdentifier(): String = dpmSourceRootPath.toString()

    override fun sourceConfigData(): String {
        return FileOps.readTextFile(dpmSourceRootPath.resolve("meta"), "source_config.json")
    }

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        val paths = FileOps.listSubFoldersMatching(dpmSourceRootPath, "dpm_dictionary_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.forEach { path ->
            val dictionarySource = DpmDictionarySourceFolderAdapter(path)
            action(dictionarySource)
        }
    }
}
