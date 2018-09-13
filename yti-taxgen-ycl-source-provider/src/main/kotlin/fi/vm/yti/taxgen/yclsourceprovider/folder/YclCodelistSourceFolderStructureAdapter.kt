package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import java.nio.file.Path

class YclCodelistSourceFolderStructureAdapter(
    index: Int,
    private val codeListPath: Path
) : YclCodelistSource(index) {

    override fun yclCodelistSourceConfigData(): String {
        return FileOps.readTextFile(codeListPath, "ycl_codelist_source_config.json")
    }

    override fun yclCodeschemeData(): String {
        return FileOps.readTextFile(codeListPath, "ycl_codescheme.json")
    }

    override fun yclCodePagesData(): Sequence<String> {
        return NumberedFilesIterator(codeListPath, "ycl_codepage_*.json").asSequence()
    }
}
