package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource

internal class CodeListSourceContextDecorator(
    private val realCodeListSource: CodeListSource,
    private val diagnosticContext: DiagnosticContext
) : CodeListSource {

    override fun blueprint(): CodeListBlueprint = realCodeListSource.blueprint()

    override fun contextTitle(): String = realCodeListSource.contextTitle()
    override fun contextIdentifier(): String = realCodeListSource.contextIdentifier()

    override fun codeListMetaData(): String = realCodeListSource.codeListMetaData()

    override fun eachCodePageData(action: (String) -> Unit) {
        realCodeListSource.eachCodePageData { pageData ->
            diagnosticContext.withContext(
                contextType = DiagnosticContexts.RdsCodesPage.toType(),
                contextDetails = null
            ) {
                action(pageData)
            }
        }
    }

    override fun eachExtensionSource(action: (ExtensionSource) -> Unit) {
        realCodeListSource.eachExtensionSource { extensionSource ->
            val decoratedExtension = ExtensionSourceContextDecorator(
                realExtensionSource = extensionSource,
                diagnosticContext = diagnosticContext
            )

            diagnosticContext.withContext(
                contextType = DiagnosticContexts.RdsExtension.toType(),
                contextDetails = decoratedExtension
            ) {
                action(decoratedExtension)
            }
        }
    }

    override fun eachSubCodeListSource(action: (CodeListSource) -> Unit) {
        realCodeListSource.eachSubCodeListSource { subCodeListSource ->
            val decoratedSubCodeListSource = CodeListSourceContextDecorator(
                realCodeListSource = subCodeListSource,
                diagnosticContext = diagnosticContext
            )

            diagnosticContext.withContext(
                contextType = DiagnosticContexts.RdsCodeList.toType(),
                contextDetails = decoratedSubCodeListSource
            ) {
                action(decoratedSubCodeListSource)
            }
        }
    }
}
