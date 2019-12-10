package fi.vm.yti.taxgen.rdsource.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.DpmSource
import fi.vm.yti.taxgen.rdsource.DpmSourceRecorder

internal class DpmSourceRecorderContextDecorator(
    private val realDpmSourceRecorder: DpmSourceRecorder,
    private val diagnosticContext: DiagnosticContext
) : DpmSourceRecorder {

    override fun contextTitle(): String = realDpmSourceRecorder.contextTitle()
    override fun contextIdentifier(): String = realDpmSourceRecorder.contextIdentifier()

    override fun captureSources(dpmSource: DpmSource) {
        diagnosticContext.withContext(
            contextType = DiagnosticContexts.DpmSourceRecorder.toType(),
            contextDetails = this
        ) {
            realDpmSourceRecorder.captureSources(dpmSource)
        }
    }

    override fun close() = realDpmSourceRecorder.close()
}
