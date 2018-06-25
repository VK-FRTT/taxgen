package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import java.nio.file.Path

class YclCodelistSourceFolderStructureAdapter(
    private val codeListPath: Path
) : YclCodelistSource {

    override fun yclCodeschemeData(): String {
        return FileOps.readTextFile(codeListPath, "ycl_codescheme.json")
    }

    override fun yclCodePagesData(): Iterator<String> {
        return NumberedFilesIterator(codeListPath, "ycl_codepage_*.json")
    }
}
