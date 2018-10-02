package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.SortOps
import java.nio.file.Path

internal class DpmDictionarySourceFolderStructureAdapter(
    private val dpmDictionaryRootPath: Path
) : DpmDictionarySource {

    override fun dpmOwnerConfigData(): String {
        return FileOps.readTextFile(dpmDictionaryRootPath, "dpm_owner_info.json")
    }

    override fun yclCodelistSources(): List<YclCodelistSource> {
        val paths = FileOps.listSubFoldersMatching(dpmDictionaryRootPath, "codelist_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.map { path -> YclCodelistSourceFolderStructureAdapter(path) }
    }
}
