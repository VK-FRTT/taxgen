package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.CodeListExtensionSource
import java.nio.file.Path

internal class CodeListExtensionSourceFolderAdapter(
    private val extensionPath: Path
) : CodeListExtensionSource {

    override fun extensionData(): String {
        return FileOps.readTextFile(extensionPath, "extension.json")
    }

    override fun extensionMemberPagesData(): Sequence<String> {
        return NumberedFilesIterator(extensionPath, "members_page_*.json").asSequence()
    }
}
