package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource
import fi.vm.yti.taxgen.rdsprovider.helpers.SortOps
import java.nio.file.Path

internal class CodeListSourceFolderAdapter(
    private val codeListRootPath: Path,
    private val blueprint: CodeListBlueprint
) : CodeListSource {

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = codeListRootPath.toString()

    override fun blueprint(): CodeListBlueprint = blueprint

    override fun codeListMetaData(): String {
        return FileOps.readTextFile(codeListRootPath, "code_list_meta.json")
    }

    override fun codePagesData(): Sequence<String> {
        return NumberedFilesIterator(codeListRootPath, "codes_page_*.json").asSequence()
    }

    override fun extensionSources(): Sequence<ExtensionSource> {
        if (!blueprint.usesExtensions) {
            return emptySequence()
        }

        val paths = FileOps.listSubFoldersMatching(codeListRootPath, "extension_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)
        return sortedPaths.map { path -> ExtensionSourceFolderAdapter(path) }.asSequence()
    }

    override fun subCodeListSources(): Sequence<CodeListSource> {
        if (!blueprint.usesSubCodeLists) {
            return emptySequence()
        }
        val paths = FileOps.listSubFoldersMatching(codeListRootPath, "sub_code_list_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)
        return sortedPaths.map { path -> CodeListSourceFolderAdapter(path, blueprint.subCodeListBlueprint!!) }
            .asSequence()
    }
}
