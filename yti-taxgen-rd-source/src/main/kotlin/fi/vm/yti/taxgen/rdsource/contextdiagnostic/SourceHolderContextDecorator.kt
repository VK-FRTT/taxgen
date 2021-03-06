package fi.vm.yti.taxgen.rdsource.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.DpmSource
import fi.vm.yti.taxgen.rdsource.SourceHolder

internal class SourceHolderContextDecorator(
    private val realSourceHolder: SourceHolder,
    private val diagnosticContext: DiagnosticContext
) : SourceHolder {

    override fun withDpmSource(action: (DpmSource) -> Unit) {

        realSourceHolder.withDpmSource { dpmSource ->
            val decoratedDpmSource = DpmSourceContextDecorator(
                realDpmSource = dpmSource,
                diagnosticContext = diagnosticContext
            )

            diagnosticContext.withContext(
                contextType = DiagnosticContexts.DpmSource.toType(),
                contextDetails = decoratedDpmSource
            ) {
                action(decoratedDpmSource)
            }
        }
    }

    override fun close() = realSourceHolder.close()
}
