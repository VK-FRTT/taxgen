package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider

abstract class YclCodelistSource(
    private val index: Int
) : DiagnosticTopicProvider {

    override fun topicType(): String = "Codelist"
    override fun topicName(): String = ""
    override fun topicIdentifier(): String = "#$index"

    abstract fun yclCodeschemeData(): String
    abstract fun yclCodePagesData(): Iterator<String>
}
