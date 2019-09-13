package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceHolder

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
                contextType = DiagnosticContextType.DpmSource,
                contextDetails = decoratedDpmSource
            ) {
                action(decoratedDpmSource)
            }
        }
    }

    override fun close() = realSourceHolder.close()
}
