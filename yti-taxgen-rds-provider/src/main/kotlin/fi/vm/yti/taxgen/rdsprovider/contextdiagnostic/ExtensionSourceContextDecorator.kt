package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource

internal class ExtensionSourceContextDecorator(
    private val extensionSource: ExtensionSource,
    private val diagnosticContext: DiagnosticContext
) : ExtensionSource {

    override fun contextLabel(): String = extensionSource.contextLabel()
    override fun contextIdentifier(): String = extensionSource.contextIdentifier()

    override fun extensionMetaData(): String = extensionSource.extensionMetaData()

    override fun eachExtensionMemberPageData(action: (String) -> Unit) {
        extensionSource.eachExtensionMemberPageData { pageData ->
            diagnosticContext.withContext(
                contextType = DiagnosticContextType.RdsExtensionMembersPage,
                contextDetails = null
            ) {
                action(pageData)
            }
        }
    }
}
