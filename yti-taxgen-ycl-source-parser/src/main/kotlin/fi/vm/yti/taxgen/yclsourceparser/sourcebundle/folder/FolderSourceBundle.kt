package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.SortOps
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
