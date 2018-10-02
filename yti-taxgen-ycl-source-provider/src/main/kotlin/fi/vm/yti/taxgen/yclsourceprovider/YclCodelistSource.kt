package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface YclCodelistSource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclCodelist
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun yclCodelistSourceConfigData(): String
    fun yclCodeSchemeData(): String
    fun yclCodePagesData(): Sequence<String>
    fun yclCodelistExtensionSources(): List<YclCodelistExtensionSource>
}
