package fi.vm.yti.taxgen.rdsource.rds

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsource.ExtensionSource

internal class ExtensionSourceRdsAdapter(
    private val extensionAddress: ExtensionAddress,
    private val rdsClient: RdsClient,
    private val diagnostic: Diagnostic
) : ExtensionSource {

    override fun contextTitle(): String = ""
    override fun contextIdentifier(): String = extensionAddress.extensionUri

    override fun extensionMetaData(): String {
        return rdsClient.fetchJsonAsString(extensionAddress.extensionUrl)
    }

    override fun eachExtensionMemberPageData(action: (String) -> Unit) {
        PaginationAwareCollectionIterator(
            extensionAddress.extensionMembersUrl,
            rdsClient,
            diagnostic
        ).forEach(action)
    }
}
