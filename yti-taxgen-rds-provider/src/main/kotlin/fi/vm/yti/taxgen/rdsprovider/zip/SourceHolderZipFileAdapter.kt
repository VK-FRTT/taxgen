package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceHolder
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
