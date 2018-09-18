package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistExtensionSource
import java.nio.file.Path

internal class YclCodelistExtensionSourceFolderStructureAdapter(
    index: Int,
    private val extensionPath: Path
) : YclCodelistExtensionSource(index) {

    override fun yclExtensionData(): String {
        return FileOps.readTextFile(extensionPath, "ycl_extension.json")
    }

    override fun yclExtensionMemberPagesData(): Sequence<String> {
        return NumberedFilesIterator(extensionPath, "ycl_extension_members_page_*.json").asSequence()
    }
}
