package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.helpers.SortOps
import java.nio.file.Path

class DpmDictionarySourceFolderStructureAdapter(
    index: Int,
    private val dpmDictionaryRootPath: Path
) : DpmDictionarySource(index) {

    override fun dpmOwnerConfigData(): String {
        return FileOps.readTextFile(dpmDictionaryRootPath, "dpm_owner_info.json")
    }

    override fun yclCodelistSources(): List<YclCodelistSource> {
        val paths = FileOps.listSubFoldersMatching(dpmDictionaryRootPath, "codelist_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.mapIndexed { index, path -> YclCodelistSourceFolderStructureAdapter(index, path) }
    }
}
