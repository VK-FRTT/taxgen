package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps

internal class ExtensionSourceRdsAdapter(
    private val extensionUrls: ExtensionUrls,
    private val diagnostic: Diagnostic
) : ExtensionSource {

    override fun extensionMetaData(): String {
        return HttpOps.fetchJsonData(extensionUrls.extensionUrl, diagnostic)
    }

    override fun extensionMemberPagesData(): Sequence<String> {
        return PaginationAwareCollectionIterator(
            extensionUrls.extensionMembersUrl,
            diagnostic,
            DiagnosticContextType.RdsExtensionMembersPage
        ).asSequence()
    }
}
