package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.Severity.ERROR
import fi.vm.yti.taxgen.commons.diagostic.Severity.FATAL
import fi.vm.yti.taxgen.commons.diagostic.Severity.INFO
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import java.util.LinkedList

class DiagnosticBridge(
    private val consumer: DiagnosticConsumer
) : Diagnostic {
    private val contextStack = LinkedList<ContextInfo>()
    private val counters = Severity.values().map { it -> Pair(it, 0) }.toMap().toMutableMap()

    override fun <R> withContext(
        diagnosticContext: DiagnosticContextProvider,
        block: () -> R
    ): R {
        return withContext(
            diagnosticContext.contextType(),
            diagnosticContext.contextName(),
            diagnosticContext.contextRef(),
            block
        )
    }

    override fun <R> withContext(
        contextType: DiagnosticContextType,
        contextName: String,
        contextRef: String,
        block: () -> R
    ): R {
        val info = ContextInfo(
            type = contextType,
            name = contextName,
            ref = contextRef
        )

        contextStack.push(info)
        consumer.contextEnter(contextStack)

        val ret = block()

        val retired = contextStack.pop()
        consumer.contextExit(contextStack, retired)

        return ret
    }

    override fun updateCurrentContextName(name: String?) {
        if (name == null) {
            return
        }

        val original = contextStack.peekFirst()

        if (original != null) {
            val updated = original.copy(name = name)
            contextStack[0] = updated
            consumer.topContextNameChange(contextStack, original)
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
