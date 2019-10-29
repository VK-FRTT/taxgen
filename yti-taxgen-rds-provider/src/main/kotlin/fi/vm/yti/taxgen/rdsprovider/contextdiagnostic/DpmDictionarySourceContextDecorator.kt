package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource

internal class DpmDictionarySourceContextDecorator(
    private val realDpmDictionarySource: DpmDictionarySource,
    private val diagnosticContext: DiagnosticContext
) : DpmDictionarySource {

    override fun contextTitle(): String = realDpmDictionarySource.contextTitle()
    override fun contextIdentifier(): String = realDpmDictionarySource.contextIdentifier()

    override fun dpmOwner(action: (Owner) -> Unit) {
        realDpmDictionarySource.dpmOwner(action)
    }

    override fun metricsSource(action: (CodeListSource?) -> Unit) {
        realDpmDictionarySource.metricsSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit) {
        realDpmDictionarySource.explicitDomainsAndHierarchiesSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun explicitDimensionsSource(action: (CodeListSource?) -> Unit) {
        realDpmDictionarySource.explicitDimensionsSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun typedDomainsSource(action: (CodeListSource?) -> Unit) {
        realDpmDictionarySource.typedDomainsSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun typedDimensionsSource(action: (CodeListSource?) -> Unit) {
        realDpmDictionarySource.typedDimensionsSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    private fun decorateAndPerformAction(
        codeListSource: CodeListSource?,
        action: (CodeListSource?) -> Unit
    ) {
        val decoratedSource = if (codeListSource == null) {
            null
        } else {
            CodeListSourceContextDecorator(
                realCodeListSource = codeListSource,
                diagnosticContext = diagnosticContext
            )
        }

        diagnosticContext.withContext(
            contextType = DiagnosticContexts.RdsCodeList.toType(),
            contextDetails = decoratedSource
        ) {
            action(decoratedSource)
        }
    }
}
