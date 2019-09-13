package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource

internal class ExtensionSourceContextDecorator(
    private val realExtensionSource: ExtensionSource,
    private val diagnosticContext: DiagnosticContext
) : ExtensionSource {

    override fun contextLabel(): String = realExtensionSource.contextLabel()
    override fun contextIdentifier(): String = realExtensionSource.contextIdentifier()

    override fun extensionMetaData(): String = realExtensionSource.extensionMetaData()

    override fun eachExtensionMemberPageData(action: (String) -> Unit) {
        realExtensionSource.eachExtensionMemberPageData { pageData ->
            diagnosticContext.withContext(
                contextType = DiagnosticContextType.RdsExtensionMembersPage,
                contextDetails = null
            ) {
                action(pageData)
            }
        }
    }
}
