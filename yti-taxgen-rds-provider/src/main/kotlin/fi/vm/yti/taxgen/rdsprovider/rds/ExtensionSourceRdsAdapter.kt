package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps

internal class ExtensionSourceRdsAdapter(
    private val extensionAddress: ExtensionAddress,
    private val diagnostic: Diagnostic
) : ExtensionSource {

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = extensionAddress.extensionUri

    override fun extensionMetaData(): String {
        return HttpOps.fetchJsonData(extensionAddress.extensionUrl, diagnostic)
    }

    override fun extensionMemberPagesData(): Sequence<String> {
        return PaginationAwareCollectionIterator(
            extensionAddress.extensionMembersUrl,
            diagnostic,
            DiagnosticContextType.RdsExtensionMembersPage
        ).asSequence()
    }
}
