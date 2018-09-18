package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistExtensionSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps

internal class YclCodelistExtensionSourceApiAdapter(
    index: Int,
    private val extensionUrls: ExtensionUrls,
    private val diagnostic: Diagnostic
) : YclCodelistExtensionSource(index) {

    override fun yclExtensionData(): String {
        return HttpOps.fetchJsonData(extensionUrls.extensionUrl, diagnostic)
    }

    override fun yclExtensionMemberPagesData(): Sequence<String> {
        return YclPaginationAwareResourceIterator(
            extensionUrls.extensionMembersUrl,
            diagnostic,
            DiagnosticContextType.YclCodelistExtensionMembersPage
        ).asSequence()
    }
}
