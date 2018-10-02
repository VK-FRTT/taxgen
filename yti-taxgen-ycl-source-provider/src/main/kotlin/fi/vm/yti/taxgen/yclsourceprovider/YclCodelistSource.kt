package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface YclCodelistSource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclCodelist
    override fun contextName(): String = ""
    override fun contextRef(): String = ""

    fun yclCodelistSourceConfigData(): String
    fun yclCodeSchemeData(): String
    fun yclCodePagesData(): Sequence<String>
    fun yclCodelistExtensionSources(): List<YclCodelistExtensionSource>
}
