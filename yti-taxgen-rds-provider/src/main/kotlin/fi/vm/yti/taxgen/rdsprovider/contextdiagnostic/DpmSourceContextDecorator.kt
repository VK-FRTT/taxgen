package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource

internal class DpmSourceContextDecorator(
    private val realDpmSource: DpmSource,
    private val diagnosticContext: DiagnosticContext
) : DpmSource {

    override fun contextLabel() = realDpmSource.contextLabel()
    override fun contextIdentifier() = realDpmSource.contextIdentifier()
    override fun sourceConfigData(): String = realDpmSource.sourceConfigData()

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {

        realDpmSource.eachDpmDictionarySource { dictionarySource ->

            val decoratedSource = DpmDictionarySourceContextDecorator(
                realDpmDictionarySource = dictionarySource,
                diagnosticContext = diagnosticContext
            )

            diagnosticContext.withContext(
                contextType = DiagnosticContextType.DpmDictionary,
                contextDetails = decoratedSource
            ) {
                action(decoratedSource)
            }
        }
    }
}
