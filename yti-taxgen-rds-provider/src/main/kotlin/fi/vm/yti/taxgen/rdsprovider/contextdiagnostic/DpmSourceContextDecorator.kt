package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource

internal class DpmSourceContextDecorator(
    private val dpmSource: DpmSource,
    private val diagnosticContext: DiagnosticContext
) : DpmSource {

    override fun contextLabel() = dpmSource.contextLabel()
    override fun contextIdentifier() = dpmSource.contextIdentifier()
    override fun sourceConfigData(): String = dpmSource.sourceConfigData()

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        dpmSource.eachDpmDictionarySource { dictionarySource ->

            val decoratedSource = DpmDictionarySourceContextDecorator(dictionarySource, diagnosticContext)

            diagnosticContext.withContext(
                contextType = DiagnosticContextType.DpmDictionary,
                contextDetails = decoratedSource
            ) {
                action(decoratedSource)
            }
        }
    }

    override fun close() = dpmSource.close()
}
