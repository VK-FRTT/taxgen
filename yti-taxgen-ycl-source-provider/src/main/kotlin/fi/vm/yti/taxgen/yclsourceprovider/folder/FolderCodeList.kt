package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.yclsourceprovider.CodeList
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import java.nio.file.Path

class FolderCodeList(
    private val codeListPath: Path
) : CodeList {

    override fun codeListData(): String {
        return FileOps.readTextFile(codeListPath, "codelist.json")
    }

    override fun codePagesData(): Iterator<String> {
        return FolderPagedResourceIterator(codeListPath, "codepage_*.json")
    }
}
