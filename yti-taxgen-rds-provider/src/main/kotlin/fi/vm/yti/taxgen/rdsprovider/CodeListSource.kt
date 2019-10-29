package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails

interface CodeListSource : DiagnosticContextDetails {
    fun blueprint(): CodeListBlueprint
    fun codeListMetaData(): String
    fun eachCodePageData(action: (String) -> Unit)
    fun eachExtensionSource(action: (ExtensionSource) -> Unit)
    fun eachSubCodeListSource(action: (CodeListSource) -> Unit)
}
