package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceProvider

internal class SourceProviderContextDecorator(
    private val sourceProvider: SourceProvider,
    private val diagnosticContext: DiagnosticContext
) : SourceProvider {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        sourceProvider.withDpmSource { dpmSource ->
            val decoratedDpmSource = DpmSourceContextDecorator(
                dpmSource = dpmSource,
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

    override fun close() = sourceProvider.close()
}
