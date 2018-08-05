package fi.vm.yti.taxgen.commons.diagostic

interface DiagnosticTopicProvider {
    fun topicType(): String
    fun topicName(): String
    fun topicIdentifier(): String
}
