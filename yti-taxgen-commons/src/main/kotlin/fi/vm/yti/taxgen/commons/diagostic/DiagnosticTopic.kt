package fi.vm.yti.taxgen.commons.diagostic

//TODO - consider the need
data class DiagnosticTopic(
    private val type: String = "",
    private val name: String = "",
    private val identifier: String = ""
) : DiagnosticTopicProvider {
    override fun topicType(): String = type
    override fun topicName(): String = name
    override fun topicIdentifier(): String = identifier
}
