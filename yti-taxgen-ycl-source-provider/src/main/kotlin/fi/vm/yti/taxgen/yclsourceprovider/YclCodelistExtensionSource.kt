package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface YclCodelistExtensionSource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclCodelistExtension
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun yclExtensionData(): String
    fun yclExtensionMemberPagesData(): Sequence<String>
}
