package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails

interface ExtensionSource : DiagnosticContextDetails {
    fun extensionMetaData(): String
    fun eachExtensionMemberPageData(action: (String) -> Unit)
}
