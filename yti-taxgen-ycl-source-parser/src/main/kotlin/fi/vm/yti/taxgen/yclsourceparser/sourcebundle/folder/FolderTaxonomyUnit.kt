package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.SortOps
import java.nio.file.Path

class FolderTaxonomyUnit(
    private val taxonomyUnitPath: Path
) : TaxonomyUnit {

    override fun taxonomyUnitInfoData(): String {
        return FileOps.readTextFile(taxonomyUnitPath, "taxonomyunit_info.json")
    }

    override fun codeLists(): List<CodeList> {
        val paths = FileOps.listSubFoldersMatching(taxonomyUnitPath, "codelist_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.map { FolderCodeList(it) }
    }
}
