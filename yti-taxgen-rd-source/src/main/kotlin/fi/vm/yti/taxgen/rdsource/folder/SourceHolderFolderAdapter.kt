package fi.vm.yti.taxgen.rdsource.folder

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.DpmSource
import fi.vm.yti.taxgen.rdsource.SourceHolder
import java.nio.file.Path

class SourceHolderFolderAdapter(
    private val dpmSourceRootPath: Path,
    private val diagnosticContext: DiagnosticContext
) : SourceHolder {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceFolderAdapter(
            dpmSourceRootPath,
            diagnosticContext
        )

        action(dpmSource)
    }

    override fun close() {}
}
