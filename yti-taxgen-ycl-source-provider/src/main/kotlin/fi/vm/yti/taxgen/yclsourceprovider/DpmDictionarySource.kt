package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider

abstract class DpmDictionarySource(
    private val index: Int
) : DiagnosticTopicProvider {

    override fun topicType(): String = "DPM Dictionary"
    override fun topicName(): String = ""
    override fun topicIdentifier(): String = "#$index"

    abstract fun dpmOwnerConfigData(): String
    abstract fun yclCodelistSources(): List<YclCodelistSource>
}
