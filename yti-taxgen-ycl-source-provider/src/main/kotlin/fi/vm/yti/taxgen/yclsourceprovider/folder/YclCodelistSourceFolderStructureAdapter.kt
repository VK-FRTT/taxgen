package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistExtensionSource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.SortOps
import java.nio.file.Path

internal class YclCodelistSourceFolderStructureAdapter(
    private val codelistPath: Path
) : YclCodelistSource {

    override fun yclCodelistSourceConfigData(): String {
        return FileOps.readTextFile(codelistPath, "ycl_codelist_source_config.json")
    }

    override fun yclCodeSchemeData(): String {
        return FileOps.readTextFile(codelistPath, "ycl_codescheme.json")
    }

    override fun yclCodePagesData(): Sequence<String> {
        return NumberedFilesIterator(codelistPath, "ycl_codes_page_*.json").asSequence()
    }

    override fun yclCodelistExtensionSources(): List<YclCodelistExtensionSource> {
        val paths = FileOps.listSubFoldersMatching(codelistPath, "extension_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)
        return sortedPaths.map { path -> YclCodelistExtensionSourceFolderStructureAdapter(path) }
    }
}
