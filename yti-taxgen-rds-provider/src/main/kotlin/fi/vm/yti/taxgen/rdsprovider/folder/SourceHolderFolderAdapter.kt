package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceHolder
import java.nio.file.Path

class SourceHolderFolderAdapter(
    private val dpmSourceRootPath: Path,
    private val diagnostic: Diagnostic
) : SourceHolder {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceFolderAdapter(
            dpmSourceRootPath,
            diagnostic
        )

        action(dpmSource)
    }

    override fun close() {}
}
