package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface CodeListSource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.RdsCodeList
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun blueprint(): CodeListBlueprint
    fun codeListMetaData(): String
    fun codePagesData(): Sequence<String>
    fun extensionSources(): Sequence<ExtensionSource>
    fun subCodeListSources(): Sequence<CodeListSource>
}
