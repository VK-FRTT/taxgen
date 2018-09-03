package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider

abstract class YclCodelistSource(
    private val index: Int
) : DiagnosticContextProvider {

    override fun contextType(): String = "Codelist"
    override fun contextName(): String = ""
    override fun contextRef(): String = "#$index"

    abstract fun yclCodeschemeData(): String
    abstract fun yclCodePagesData(): Sequence<String>
}
