package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import java.nio.file.Path

class FolderCodeList(
    private val codeListPath: Path
) : CodeList {

    override fun codeList(): String {
        return FileOps.readTextFile(codeListPath, "codelist.json")
    }

    override fun codesPages(): Iterator<String> {
        return FolderPagedResourceIterator(codeListPath, "codespage_*.json")
    }
}
