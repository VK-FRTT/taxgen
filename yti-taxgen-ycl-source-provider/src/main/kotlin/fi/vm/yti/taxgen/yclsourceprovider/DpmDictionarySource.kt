package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

abstract class DpmDictionarySource(
    private val index: Int
) : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.DpmDictionary
    override fun contextName(): String = ""
    override fun contextRef(): String = "#$index"

    abstract fun dpmOwnerConfigData(): String
    abstract fun yclCodelistSources(): List<YclCodelistSource>
}
