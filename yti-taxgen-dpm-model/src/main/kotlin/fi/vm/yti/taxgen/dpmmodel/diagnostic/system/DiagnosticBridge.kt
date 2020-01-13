package fi.vm.yti.taxgen.dpmmodel.diagnostic.system

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.DEBUG
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.ERROR
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.FATAL
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.INFO
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.WARNING
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationResultCollector
import java.util.LinkedList

class DiagnosticBridge(
    private val eventConsumer: DiagnosticEventConsumer,
    private val stoppingPolicy: DiagnosticProcesStoppingPolicy,
    private val eventFilteringPolicy: DiagnosticEventFilteringPolicy
) : DiagnosticContext {
    private val contextStack = LinkedList<DiagnosticContextDescriptor>()
    private var previousRetiredContext: DiagnosticContextDescriptor? = null
    private val counters = Severity.values().map { it -> Pair(it, 0) }.toMap().toMutableMap()
    private var diagnosticSourceLanguages = emptyList<Language>()

    override fun <R> withContext(
        contextType: DiagnosticContextType,
        contextDetails: DiagnosticContextDetails?,
        action: () -> R
    ): R {
        val recurrenceIndex =
            previousRetiredContext?.let { if (it.contextType.typeName == contextType.typeName) it.recurrenceIndex + 1 else null }
                ?: 0

        val context = DiagnosticContextDescriptor(
            contextType = contextType,
            recurrenceIndex = recurrenceIndex,
            contextTitle = contextDetails?.contextTitle() ?: "",
            contextIdentifier = contextDetails?.contextIdentifier() ?: ""
        )

        contextStack.push(context)
        eventConsumer.contextEnter(contextStack)

        try {
            val ret = action()

            val retired = contextStack.pop()
            previousRetiredContext = retired
            eventConsumer.contextExit(contextStack, retired)

            return ret
        } catch (ex: Exception) {
            stoppingPolicy.exceptionCaughtStopProcessing(ex, this)
        }
    }

    override fun criticalErrorsReceived(): Boolean = counters[FATAL] != 0 || counters[ERROR] != 0

    override fun stopIfCriticalErrorsReceived(messageProvider: () -> String) {
        if (criticalErrorsReceived()) {
            val message = messageProvider()
            info(message)

            stoppingPolicy.stopProcessing()
        }
    }

    override fun updateCurrentContextDetails(contextTitle: String?, contextIdentifier: String?) {
        val original = contextStack.peekFirst()

        if (original != null) {
            val newTitle = contextTitle ?: original.contextTitle
            val newIdentifier = contextIdentifier ?: original.contextIdentifier

            val updated = original.copy(
                contextTitle = newTitle,
                contextIdentifier = newIdentifier
            )

            contextStack[0] = updated
            eventConsumer.topContextDetailsChange(contextStack, original)
        }
    }

    override fun fatal(message: String): Nothing {
        processMessage(FATAL, message)
        stoppingPolicy.stopProcessing()
    }

    override fun error(message: String) {
        processMessage(ERROR, message)
    }

    override fun warning(message: String) {
        processMessage(WARNING, message)
    }

    override fun info(message: String) {
        processMessage(INFO, message)
    }

    override fun debug(message: String) {
        processMessage(DEBUG, message)
    }

    override fun validate(
        validatable: Validatable
    ) {
        validate(listOf(validatable))
    }

    override fun validate(
        validatables: List<Validatable>
    ) {
        val resultCollector = ValidationResultCollector()

        validatables.forEach {
            resultCollector.withSubject(it.validationSubjectDescriptor()) {
                it.validate(resultCollector)
            }
        }

        val results = resultCollector.uniqueResults()

        if (results.any()) {
            incrementCounter(ERROR)
            eventConsumer.validationResults(results)
        }
    }

    override fun diagnosticSourceLanguages(): List<Language> = diagnosticSourceLanguages

    fun setDiagnosticSourceLanguages(sourceLanguages: List<Language>) {
        diagnosticSourceLanguages = sourceLanguages.toCollection(mutableListOf())
    }

    private fun processMessage(severity: Severity, message: String) {
        incrementCounter(severity)

        if (!eventFilteringPolicy.suppressMessage(severity, message)) {
            eventConsumer.message(severity, message)
        }
    }

    private fun incrementCounter(severity: Severity) {
        val current = counters.getValue(severity)
        counters[severity] = current + 1
    }
}
