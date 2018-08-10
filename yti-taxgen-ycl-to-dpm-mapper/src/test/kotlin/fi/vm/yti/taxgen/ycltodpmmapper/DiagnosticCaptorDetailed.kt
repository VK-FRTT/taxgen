package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.diagostic.TopicInfo

class DiagnosticCaptorDetailed : DiagnosticConsumer {
    val events = mutableListOf<String>()

    fun topicToString(topic: TopicInfo): String = "TOPIC{${topic.type},${topic.name},${topic.identifier}}"

    override fun topicEnter(topicStack: List<TopicInfo>) {
        events.add("ENTER [${topicStack.joinToString { topicToString(it) }}]")
    }

    override fun topicExit(topicStack: List<TopicInfo>, retiredTopic: TopicInfo) {
        events.add("EXIT [${topicStack.joinToString { topicToString(it) }}] RETIRED [${topicToString(retiredTopic)}]")
    }

    override fun topmostTopicNameUpdate(topicStack: List<TopicInfo>, originalTopic: TopicInfo) {
        events.add(
            "UPDATE [${topicStack.joinToString { topicToString(it) }}] ORIGINAL [${topicToString(
                originalTopic
            )}]"
        )
    }

    override fun message(severity: Severity, message: String) {
        events.add("MESSAGE [$severity] MESSAGE [$message]")
    }

    override fun validationErrors(validationErrors: ValidationErrors) {
        events.add("VALIDATION [${validationErrors.errorsInSimpleFormat()}]")
    }
}
