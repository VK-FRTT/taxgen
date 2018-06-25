package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.helpers.SortOps
import java.nio.file.Path

class DpmDictionarySourceFolderStructureAdapter(
    private val dpmDictionaryRootPath: Path
) : DpmDictionarySource {

    override fun dpmOwnerInfoData(): String {
        return FileOps.readTextFile(dpmDictionaryRootPath, "dpm_owner_info.json")
    }

    override fun yclCodelistSources(): List<YclCodelistSource> {
        val paths = FileOps.listSubFoldersMatching(dpmDictionaryRootPath, "codelist_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.map { YclCodelistSourceFolderStructureAdapter(it) }
    }
}
