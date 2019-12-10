package fi.vm.yti.taxgen.rddpmmapper.modelmapper

import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsExtensionMember
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsExtensionMembersPage
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsExtensionMeta
import fi.vm.yti.taxgen.rdsource.ExtensionSource

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
