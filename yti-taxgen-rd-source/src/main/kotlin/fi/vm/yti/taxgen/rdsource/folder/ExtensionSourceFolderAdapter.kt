package fi.vm.yti.taxgen.rdsource.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsource.ExtensionSource
import java.nio.file.Path

internal class ExtensionSourceFolderAdapter(
    private val extensionPath: Path,
    private val diagnostic: Diagnostic
) : ExtensionSource {

    override fun contextTitle(): String = ""
    override fun contextIdentifier(): String = extensionPath.toString()

    override fun extensionMetaData(): String {
        return FileOps.readTextFile(extensionPath, "extension_meta.json")
    }

    override fun eachExtensionMemberPageData(action: (String) -> Unit) {
        NumberedFilesIterator(
            extensionPath,
            "members_page_*.json",
            diagnostic
        ).forEach(action)
    }
}
