package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.DpmSourceConfigHolder

internal class DpmSourceContextDecorator(
    private val realDpmSource: DpmSource,
    private val diagnosticContext: DiagnosticContext
) : DpmSource {

    override fun contextTitle() = realDpmSource.contextTitle()
    override fun contextIdentifier() = realDpmSource.contextIdentifier()
    override fun config(): DpmSourceConfigHolder = realDpmSource.config()

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {

        realDpmSource.eachDpmDictionarySource { dictionarySource ->

            val decoratedSource = DpmDictionarySourceContextDecorator(
                realDpmDictionarySource = dictionarySource,
                diagnosticContext = diagnosticContext
            )

            diagnosticContext.withContext(
                contextType = DiagnosticContexts.DpmDictionary.toType(),
                contextDetails = decoratedSource
            ) {
                action(decoratedSource)
            }
        }
    }
}
