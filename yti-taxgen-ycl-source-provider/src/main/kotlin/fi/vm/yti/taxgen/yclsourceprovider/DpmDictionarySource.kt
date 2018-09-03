package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider

abstract class DpmDictionarySource(
    private val index: Int
) : DiagnosticContextProvider {

    override fun contextType(): String = "DPM Dictionary"
    override fun contextName(): String = ""
    override fun contextRef(): String = "#$index"

    abstract fun dpmOwnerConfigData(): String
    abstract fun yclCodelistSources(): List<YclCodelistSource>
}
