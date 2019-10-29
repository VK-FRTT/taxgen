package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource

internal class ExtensionSourceContextDecorator(
    private val realExtensionSource: ExtensionSource,
    private val diagnosticContext: DiagnosticContext
) : ExtensionSource {

    override fun contextTitle(): String = realExtensionSource.contextTitle()
    override fun contextIdentifier(): String = realExtensionSource.contextIdentifier()

    override fun extensionMetaData(): String = realExtensionSource.extensionMetaData()

    override fun eachExtensionMemberPageData(action: (String) -> Unit) {
        realExtensionSource.eachExtensionMemberPageData { pageData ->
            diagnosticContext.withContext(
                contextType = DiagnosticContexts.RdsExtensionMembersPage.toType(),
                contextDetails = null
            ) {
                action(pageData)
            }
        }
    }
}
