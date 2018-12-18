package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.CodeListExtensionSource
import java.nio.file.Path

internal class CodeListExtensionSourceFolderAdapter(
    private val extensionPath: Path
) : CodeListExtensionSource {

    override fun extensionMetaData(): String {
        return FileOps.readTextFile(extensionPath, "extension_meta.json")
    }

    override fun extensionMemberPagesData(): Sequence<String> {
        return NumberedFilesIterator(extensionPath, "members_page_*.json").asSequence()
    }
}
