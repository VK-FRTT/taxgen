package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

abstract class YclCodelistSource(
    private val index: Int
) : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclCodelist
    override fun contextName(): String = ""
    override fun contextRef(): String = "#$index"

    abstract fun yclCodelistSourceConfigData(): String
    abstract fun yclCodeschemeData(): String
    abstract fun yclCodePagesData(): Sequence<String>
}
