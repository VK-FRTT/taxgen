package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface CodeListSource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.RdsCodelist
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun codeListData(): String
    fun codePagesData(): Sequence<String>
    fun extensionSources(): Sequence<CodeListExtensionSource>
    fun subCodeListSources(): Sequence<CodeListSource>
}
