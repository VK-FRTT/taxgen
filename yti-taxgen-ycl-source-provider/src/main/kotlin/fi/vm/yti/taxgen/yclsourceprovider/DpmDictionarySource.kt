package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface DpmDictionarySource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.DpmDictionary
    override fun contextName(): String = ""
    override fun contextRef(): String = ""

    fun dpmOwnerConfigData(): String
    fun yclCodelistSources(): List<YclCodelistSource>
}
