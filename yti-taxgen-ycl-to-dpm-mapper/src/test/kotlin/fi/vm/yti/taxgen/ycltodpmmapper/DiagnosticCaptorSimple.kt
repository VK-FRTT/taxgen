package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.diagostic.TopicInfo

class DiagnosticCaptorSimple : DiagnosticConsumer {

    val events = mutableListOf<String>()

    override fun topicEnter(topicStack: List<TopicInfo>) {
        events.add("ENTER [${topicStack.firstOrNull()?.type ?: ""}]")
    }

    override fun topicExit(topicStack: List<TopicInfo>, retiredTopic: TopicInfo) {
        events.add("EXIT [${topicStack.firstOrNull()?.type ?: ""}] RETIRED [${retiredTopic.type}]")
    }

    override fun topmostTopicNameUpdate(topicStack: List<TopicInfo>, originalTopic: TopicInfo) {
        events.add("UPDATE [${topicStack.firstOrNull()?.type ?: ""}] ORIGINAL [${originalTopic.type}]")
    }

    override fun message(severity: Severity, message: String) {
        events.add("MESSAGE [$severity] MESSAGE [$message]")
    }

    override fun validationErrors(validationErrors: ValidationErrors) {
        events.add("VALIDATION [${validationErrors.errorsInSimpleFormat()}]")
    }
}
