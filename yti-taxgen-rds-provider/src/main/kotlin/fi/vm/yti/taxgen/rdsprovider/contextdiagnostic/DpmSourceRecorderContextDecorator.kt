package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.SourceProvider

internal class DpmSourceRecorderContextDecorator(
    private val dpmSourceRecorder: DpmSourceRecorder,
    private val diagnosticContext: DiagnosticContext
) : DpmSourceRecorder {

    override fun contextLabel(): String = dpmSourceRecorder.contextLabel()
    override fun contextIdentifier(): String = dpmSourceRecorder.contextIdentifier()

    override fun captureSources(sourceProvider: SourceProvider) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.CaptureDpmSource,
            contextDetails = this
        ) {
            dpmSourceRecorder.captureSources(sourceProvider)
        }
    }

    override fun close() = dpmSourceRecorder.close()
}
