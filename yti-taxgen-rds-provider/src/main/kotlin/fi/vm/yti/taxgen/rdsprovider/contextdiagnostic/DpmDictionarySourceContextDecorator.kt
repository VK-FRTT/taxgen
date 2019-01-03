package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource

internal class DpmDictionarySourceContextDecorator(
    private val dpmDictionarySource: DpmDictionarySource,
    private val diagnosticContext: DiagnosticContext
) : DpmDictionarySource {

    override fun contextLabel(): String = dpmDictionarySource.contextLabel()
    override fun contextIdentifier(): String = dpmDictionarySource.contextIdentifier()

    override fun dpmOwnerConfigData(action: (String) -> Unit) {
        dpmDictionarySource.dpmOwnerConfigData(action)
    }

    override fun metricsSource(action: (CodeListSource?) -> Unit) {
        dpmDictionarySource.metricsSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit) {
        dpmDictionarySource.explicitDomainsAndHierarchiesSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun explicitDimensionsSource(action: (CodeListSource?) -> Unit) {
        dpmDictionarySource.explicitDimensionsSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun typedDomainsSource(action: (CodeListSource?) -> Unit) {
        dpmDictionarySource.typedDomainsSource { codeListSource ->
            decorateAndPerformAction(codeListSource, action)
        }
    }

    override fun typedDimensionsSource(action: (CodeListSource?) -> Unit) {
        dpmDictionarySource.typedDimensionsSource { codeListSource ->
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
            CodeListSourceContextDecorator(codeListSource, diagnosticContext)
        }

        diagnosticContext.withContext(
            contextType = DiagnosticContextType.RdsCodeList,
            contextDetails = decoratedSource
        ) {
            action(decoratedSource)
        }
    }
}
