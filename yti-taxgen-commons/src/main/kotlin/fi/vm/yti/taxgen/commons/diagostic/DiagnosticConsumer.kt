package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors

interface DiagnosticConsumer {

    fun topicEnter(topicStack: List<TopicInfo>)
    fun topicExit(topicStack: List<TopicInfo>, retiredTopic: TopicInfo)
    fun topTopicNameUpdated(topicStack: List<TopicInfo>, originalTopic: TopicInfo)

    fun message(severity: Severity, message: String)
    fun validationErrors(validationErrors: ValidationErrors)
}
