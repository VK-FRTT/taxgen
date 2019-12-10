package fi.vm.yti.taxgen.rdsource.zip

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.DpmSource
import fi.vm.yti.taxgen.rdsource.SourceHolder
import java.nio.file.Path

class SourceHolderZipFileAdapter(
    private val sourceZipPath: Path,
    private val diagnosticContext: DiagnosticContext
) : SourceHolder {

    private val dpmSource: DpmSourceZipFileAdapter by lazy {
        DpmSourceZipFileAdapter(
            sourceZipPath,
            diagnosticContext
        )
    }

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        action(dpmSource)
    }

    override fun close() {
        dpmSource.close()
    }
}
