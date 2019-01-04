package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
import java.nio.file.Path

class SourceProviderFolderAdapter(
    private val dpmSourceRootPath: Path
) : SourceProvider {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceFolderAdapter(dpmSourceRootPath)
        action(dpmSource)
    }

    override fun close() {}
}
