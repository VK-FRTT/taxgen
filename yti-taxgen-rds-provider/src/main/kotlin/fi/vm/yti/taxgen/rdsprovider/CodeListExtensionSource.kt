package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface CodeListExtensionSource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.RdsCodelistExtension
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun extensionMetaData(): String
    fun extensionMemberPagesData(): Sequence<String>
}
