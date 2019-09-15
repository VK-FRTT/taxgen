package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource
import java.nio.file.Path

internal class ExtensionSourceFolderAdapter(
    private val extensionPath: Path
) : ExtensionSource {

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = extensionPath.toString()

    override fun extensionMetaData(): String {
        return FileOps.readTextFile(extensionPath, "extension_meta.json")
    }

    override fun eachExtensionMemberPageData(action: (String) -> Unit) {
        NumberedFilesIterator(
            extensionPath,
            "members_page_*.json"
        ).forEach(action)
    }
}
