package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.yclsourceprovider.SourceBundle
import fi.vm.yti.taxgen.yclsourceprovider.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.helpers.SortOps
import java.nio.file.Path

class FolderSourceBundle(
    baseFolderPath: Path
) : SourceBundle {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun bundleInfoData(): String {
        return FileOps.readTextFile(baseFolderPath, "bundle_info.json")
    }

    override fun taxonomyUnits(): List<TaxonomyUnit> {
        val paths = FileOps.listSubFoldersMatching(baseFolderPath, "taxonomyunit_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.map { FolderTaxonomyUnit(it) }
    }

    override fun close() {}
}
