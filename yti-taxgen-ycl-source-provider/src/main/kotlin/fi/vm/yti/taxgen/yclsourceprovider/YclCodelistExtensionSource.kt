package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

abstract class YclCodelistExtensionSource(
    private val index: Int
) : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclCodelistExtension
    override fun contextName(): String = ""
    override fun contextRef(): String = "#$index"

    abstract fun yclExtensionData(): String
    abstract fun yclExtensionMemberPagesData(): Sequence<String>
}
