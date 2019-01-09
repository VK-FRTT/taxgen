package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource

internal class CodeListSourceContextDecorator(
    private val codeListSource: CodeListSource,
    private val diagnosticContext: DiagnosticContext
) : CodeListSource {

    override fun blueprint(): CodeListBlueprint = codeListSource.blueprint()

    override fun contextLabel(): String = codeListSource.contextLabel()
    override fun contextIdentifier(): String = codeListSource.contextIdentifier()

    override fun codeListMetaData(): String = codeListSource.codeListMetaData()

    override fun eachCodePageData(action: (String) -> Unit) {
        codeListSource.eachCodePageData { pageData ->
            diagnosticContext.withContext(
                contextType = DiagnosticContextType.RdsCodesPage,
                contextDetails = null
            ) {
                action(pageData)
            }
        }
    }

    override fun eachExtensionSource(action: (ExtensionSource) -> Unit) {
        codeListSource.eachExtensionSource { extensionSource ->
            val decoratedExtension = ExtensionSourceContextDecorator(extensionSource, diagnosticContext)

            diagnosticContext.withContext(
                contextType = DiagnosticContextType.RdsExtension,
                contextDetails = decoratedExtension
            ) {
                action(decoratedExtension)
            }
        }
    }

    override fun eachSubCodeListSource(action: (CodeListSource) -> Unit) {
        codeListSource.eachSubCodeListSource { subCodeListSource ->
            val decoratedSubCodeListSource = CodeListSourceContextDecorator(subCodeListSource, diagnosticContext)

            diagnosticContext.withContext(
                contextType = DiagnosticContextType.RdsCodeList,
                contextDetails = decoratedSubCodeListSource
            ) {
                action(decoratedSubCodeListSource)
            }
        }
    }
}
