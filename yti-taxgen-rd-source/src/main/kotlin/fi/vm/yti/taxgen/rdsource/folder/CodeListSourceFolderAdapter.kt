package fi.vm.yti.taxgen.rdsource.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.rdsource.CodeListBlueprint
import fi.vm.yti.taxgen.rdsource.CodeListSource
import fi.vm.yti.taxgen.rdsource.ExtensionSource
import fi.vm.yti.taxgen.rdsource.helpers.SortOps
import java.nio.file.Path

internal class CodeListSourceFolderAdapter(
    private val codeListRootPath: Path,
    private val blueprint: CodeListBlueprint
) : CodeListSource {

    override fun contextTitle(): String = ""
    override fun contextIdentifier(): String = codeListRootPath.toString()

    override fun blueprint(): CodeListBlueprint = blueprint

    override fun codeListMetaData(): String {
        return FileOps.readTextFile(codeListRootPath, "code_list_meta.json")
    }

    override fun eachCodePageData(action: (String) -> Unit) {
        NumberedFilesIterator(
            codeListRootPath,
            "codes_page_*.json"
        ).forEach(action)
    }

    override fun eachExtensionSource(action: (ExtensionSource) -> Unit) {
        val paths = FileOps.listSubFoldersMatching(codeListRootPath, "extension_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        sortedPaths.forEach { path ->
            val extensionSource = ExtensionSourceFolderAdapter(path)
            action(extensionSource)
        }
    }

    override fun eachSubCodeListSource(action: (CodeListSource) -> Unit) {
        blueprint.subCodeListBlueprint ?: return

        val paths = FileOps.listSubFoldersMatching(codeListRootPath, "sub_code_list_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        sortedPaths.forEach { path ->
            val codeListSource = CodeListSourceFolderAdapter(path, blueprint.subCodeListBlueprint)

            action(codeListSource)
        }
    }
}
