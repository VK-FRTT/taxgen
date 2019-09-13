package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.SourceHolder

internal class DpmSourceRecorderContextDecorator(
    private val realDpmSourceRecorder: DpmSourceRecorder,
    private val diagnosticContext: DiagnosticContext
) : DpmSourceRecorder {

    override fun contextLabel(): String = realDpmSourceRecorder.contextLabel()
    override fun contextIdentifier(): String = realDpmSourceRecorder.contextIdentifier()

    override fun captureSources(sourceHolder: SourceHolder) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.DpmSourceRecorder,
            contextDetails = this
        ) {
            realDpmSourceRecorder.captureSources(sourceHolder)
        }
    }

    override fun close() = realDpmSourceRecorder.close()
}
