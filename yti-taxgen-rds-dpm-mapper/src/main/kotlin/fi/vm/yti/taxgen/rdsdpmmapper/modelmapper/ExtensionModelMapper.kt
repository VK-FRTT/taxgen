package fi.vm.yti.taxgen.rdsdpmmapper.modelmapper

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionMember
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionMembersPage
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionMeta
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource

internal class ExtensionModelMapper(
    private val extensionSource: ExtensionSource,
    private val diagnostic: Diagnostic
) {
    private val extensionMetaData: RdsExtensionMeta by lazy {
        JsonOps.readValue<RdsExtensionMeta>(extensionSource.extensionMetaData(), diagnostic)
    }

    fun extensionMetaData(): RdsExtensionMeta {
        return extensionMetaData
    }

    fun eachExtensionMember(action: (RdsExtensionMember) -> Unit) {
        extensionSource.eachExtensionMemberPageData { pageData ->
            val membersPage = JsonOps.readValue<RdsExtensionMembersPage>(pageData, diagnostic)
            membersPage.results?.forEach(action)
        }
    }
}
