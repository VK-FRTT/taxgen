package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationCollector
import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.diagostic.Severity.ERROR
import fi.vm.yti.taxgen.commons.diagostic.Severity.FATAL
import fi.vm.yti.taxgen.commons.diagostic.Severity.INFO
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import java.util.LinkedList

class DiagnosticBridge(
    private val consumer: DiagnosticConsumer
) : DiagnosticContext {
    private val contextStack = LinkedList<ContextInfo>()
    private var previousRetiredContext: ContextInfo? = null
    private val counters = Severity.values().map { it -> Pair(it, 0) }.toMap().toMutableMap()

    override fun <R> withContext(
        contextType: DiagnosticContextType,
        contextLabel: String,
        contextIdentifier: String,
        action: () -> R
    ): R {
        val recurrenceIndex =
            previousRetiredContext?.let { if (it.type == contextType) it.recurrenceIndex + 1 else null } ?: 0

        val info = ContextInfo(
            type = contextType,
            recurrenceIndex = recurrenceIndex,
            label = contextLabel,
            identifier = contextIdentifier
        )

        contextStack.push(info)
        consumer.contextEnter(contextStack)

        val ret = action()

        val retired = contextStack.pop()
        previousRetiredContext = retired
        consumer.contextExit(contextStack, retired)

        return ret
    }

    override fun updateCurrentContextDetails(label: String?, identifier: String?) {
        val original = contextStack.peekFirst()

        if (original != null) {
            val assignableLabel = label ?: original.label
            val assignableIdentifier = identifier ?: original.identifier

            val updated = original.copy(
                label = assignableLabel,
                identifier = assignableIdentifier
            )

            contextStack[0] = updated
            consumer.topContextDetailsChange(contextStack, original)
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

    override fun validate(
        validatable: Validatable,
        validatableInfo: ValidatableInfo?
    ) {
        val collector = ValidationCollector()
        validatable.validate(collector)

        val results = collector.compileResults()

        if (results.any()) {
            incrementCounter(ERROR)
            val info = validatableInfo ?: ValidatableInfo(
                objectKind = validatable.javaClass.simpleName,
                objectAddress = ""
            )

            consumer.validationResults(info, results)
        }
    }

    override fun counters(): Map<Severity, Int> {
        return counters
    }

    private fun incrementCounter(severity: Severity) {
        val current = counters[severity] ?: thisShouldNeverHappen("")
        counters[severity] = current + 1
    }
}
