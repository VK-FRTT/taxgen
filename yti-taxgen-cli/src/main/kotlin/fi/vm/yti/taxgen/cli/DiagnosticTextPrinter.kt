package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.diagostic.Severity.ERROR
import fi.vm.yti.taxgen.commons.diagostic.Severity.FATAL
import fi.vm.yti.taxgen.commons.diagostic.TopicInfo
import java.io.PrintWriter

class DiagnosticTextPrinter(
    private val printWriter: PrintWriter
) : DiagnosticConsumer {

    private var level = 0

    override fun topicEnter(topicStack: List<TopicInfo>) {
        level = topicStack.size
        val topic = topicStack.first()
        printTopic(topic)
    }

    override fun topicExit(topicStack: List<TopicInfo>, retiredTopic: TopicInfo) {
        printNestedLine("${retiredTopic.type} done")
        level = topicStack.size
    }

    override fun topTopicNameUpdated(topicStack: List<TopicInfo>, originalTopic: TopicInfo) {
        val topic = topicStack.first()
        printTopic(topic)
    }

    override fun message(severity: Severity, message: String) {
        when (severity) {
            FATAL, ERROR -> printNestedLine("$severity: $message")
            else -> printNestedLine(message)
        }
    }

    override fun validationErrors(validationErrors: ValidationErrors) {
        validationErrors.errorsInSimpleFormat().forEach {
            message(ERROR, it)
        }
    }

    private fun printTopic(topic: TopicInfo) {
        val line =
            (topic.type) +
                (if (topic.identifier.isNotBlank()) " (${topic.identifier})" else "") +
                (if (topic.name.isNotBlank()) " \"${topic.name}\"" else "")

        printNestedLine(line)
    }

    private fun printNestedLine(message: String) {
        printWriter.println("  ".repeat(level) + message)
    }
}
