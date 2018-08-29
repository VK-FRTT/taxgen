package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors

interface Diagnostic {
    fun topicEnter(topicProvider: DiagnosticTopicProvider)
    fun topicExit()
    fun updateCurrentTopicName(name: String?)
    fun fatal(message: String): Nothing
    fun error(message: String)
    fun info(message: String)
    fun validationErrors(validationErrors: ValidationErrors)
    fun counters(): Map<Severity, Int>

    fun <R> withinTopic(
        topicProvider: DiagnosticTopicProvider,
        block: () -> R
    ): R {
        topicEnter(topicProvider)

        val ret = block()

        topicExit()

        return ret
    }
}
