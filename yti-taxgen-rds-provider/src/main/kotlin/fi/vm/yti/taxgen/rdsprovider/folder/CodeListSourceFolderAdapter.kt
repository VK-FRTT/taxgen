package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.CodeListExtensionSource
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.helpers.SortOps
import java.nio.file.Path

internal class CodeListSourceFolderAdapter(
    private val codeListRootPath: Path
) : CodeListSource {

    override fun codeListData(): String {
        return FileOps.readTextFile(codeListRootPath, "codelist.json")
    }

    override fun codePagesData(): Sequence<String> {
        return NumberedFilesIterator(codeListRootPath, "codes_page_*.json").asSequence()
    }

    override fun extensionSources(): Sequence<CodeListExtensionSource> {
        val paths = FileOps.listSubFoldersMatching(codeListRootPath, "ext_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)
        return sortedPaths.map { path -> CodeListExtensionSourceFolderAdapter(path) }.asSequence()
    }

    override fun subCodeListSources(): Sequence<CodeListSource> {
        val paths = FileOps.listSubFoldersMatching(codeListRootPath, "sub_cl_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)
        return sortedPaths.map { path -> CodeListSourceFolderAdapter(path) }.asSequence()
    }
}
