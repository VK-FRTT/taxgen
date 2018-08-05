package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import java.nio.file.Path

class YclCodelistSourceFolderStructureAdapter(
    index: Int,
    private val codeListPath: Path
) : YclCodelistSource(index) {

    override fun yclCodeschemeData(): String {
        return FileOps.readTextFile(codeListPath, "ycl_codescheme.json")
    }

    override fun yclCodePagesData(): Iterator<String> {
        return NumberedFilesIterator(codeListPath, "ycl_codepage_*.json")
    }
}
