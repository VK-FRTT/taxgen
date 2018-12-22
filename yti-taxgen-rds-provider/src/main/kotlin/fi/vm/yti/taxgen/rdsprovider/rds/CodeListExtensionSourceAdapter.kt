package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.CodeListExtensionSource
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps

internal class CodeListExtensionSourceAdapter(
    private val extensionUrls: ExtensionUrls,
    private val diagnostic: Diagnostic
) : CodeListExtensionSource {

    override fun extensionMetaData(): String {
        return HttpOps.fetchJsonData(extensionUrls.extensionUrl, diagnostic)
    }

    override fun extensionMemberPagesData(): Sequence<String> {
        return PaginationAwareCollectionIterator(
            extensionUrls.extensionMembersUrl,
            diagnostic,
            DiagnosticContextType.RdsCodelistExtensionMembersPage
        ).asSequence()
    }
}
