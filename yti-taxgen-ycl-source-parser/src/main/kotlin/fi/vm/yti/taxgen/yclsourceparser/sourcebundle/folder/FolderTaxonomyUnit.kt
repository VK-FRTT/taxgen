package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import java.nio.file.Path

class FolderTaxonomyUnit(
    private val taxonomyUnitPath: Path
) : TaxonomyUnit {

    override fun taxonomyUnitDescriptor(): String {
        return FileOps.readTextFile(taxonomyUnitPath, "taxonomyunit.json")
    }

    override fun codeLists(): List<CodeList> {
        val codeListPaths = FileOps.listSubFoldersMatching(taxonomyUnitPath, "codelist_*")
        return codeListPaths.map { FolderCodeList(it) }
    }
}
