package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.Severity.ERROR
import fi.vm.yti.taxgen.commons.diagostic.Severity.FATAL
import fi.vm.yti.taxgen.commons.diagostic.Severity.INFO
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import java.util.LinkedList

class DiagnosticBridge(
    val consumer: DiagnosticConsumer
) : Diagnostic {
    private val topicStack = LinkedList<TopicInfo>()
    private val counters = Severity.values().map { it -> Pair(it, 0) }.toMap().toMutableMap()

    override fun topicEnter(topicProvider: DiagnosticTopicProvider) {
        val topic = TopicInfo(
            type = topicProvider.topicType(),
            name = topicProvider.topicName(),
            identifier = topicProvider.topicIdentifier()
        )

        topicStack.push(topic)
        consumer.topicEnter(topicStack)
    }

    override fun topicExit() {
        val retiredTopic = topicStack.pop()
        consumer.topicExit(topicStack, retiredTopic)
    }

    override fun updateCurrentTopicName(name: String?) {
        if (name == null) {
            return
        }

        val originalTopic = topicStack.peekFirst()

        if (originalTopic != null) {
            val newTopic = originalTopic.copy(name = name)
            topicStack[0] = newTopic
            consumer.topmostTopicNameUpdate(topicStack, originalTopic)
        }
    }

    override fun fatal(message: String): Nothing {
        incrementCounter(FATAL)
        consumer.message(FATAL, message)
        throwHalt()
    }

    override fun error(message: String) {
        incrementCounter(ERROR)
        consumer.message(ERROR, message)
    }

    override fun info(message: String) {
        incrementCounter(INFO)
        consumer.message(INFO, message)
    }

    override fun validationErrors(validationErrors: ValidationErrors) {
        incrementCounter(ERROR)
        consumer.validationErrors(validationErrors)
    }

    override fun counters(): Map<Severity, Int> {
        return counters
    }

    private fun incrementCounter(severity: Severity) {
        val current = counters[severity] ?: thisShouldNeverHappen("")
        counters[severity] = current + 1
    }
}
