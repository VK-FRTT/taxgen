package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface ExtensionSource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.RdsExtension
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun extensionMetaData(): String
    fun extensionMemberPagesData(): Sequence<String>
}
