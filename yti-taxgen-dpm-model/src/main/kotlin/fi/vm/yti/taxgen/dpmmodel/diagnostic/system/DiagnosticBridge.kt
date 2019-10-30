package fi.vm.yti.taxgen.dpmmodel.diagnostic.system

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.dpmmodel.datavalidation.system.ValidationCollector
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.ERROR
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.FATAL
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.INFO
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.WARNING
import java.util.LinkedList

class DiagnosticBridge(
    private val eventConsumer: DiagnosticEventConsumer,
    private val stoppingPolicy: DiagnosticProcesStoppingPolicy
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
        incrementCounter(FATAL)
        eventConsumer.message(FATAL, message)
        stoppingPolicy.stopProcessing()
    }

    override fun error(message: String) {
        incrementCounter(ERROR)
        eventConsumer.message(ERROR, message)
    }

    override fun warning(message: String) {
        incrementCounter(WARNING)
        eventConsumer.message(WARNING, message)
    }

    override fun info(message: String) {
        incrementCounter(INFO)
        eventConsumer.message(INFO, message)
    }

    override fun validate(
        validatable: Validatable
    ) {
        validate(validatable) {
            ValidatableInfo(
                objectKind = validatable.javaClass.simpleName,
                objectAddress = ""
            )
        }
    }

    override fun validate(
        validatable: Validatable,
        infoProvider: () -> ValidatableInfo
    ) {
        val collector = ValidationCollector()
        validatable.validate(collector)

        val results = collector.compileResults()

        if (results.any()) {
            incrementCounter(ERROR)
            val info = infoProvider()
            eventConsumer.validationResults(info, results)
        }
    }

    override fun diagnosticSourceLanguages(): List<Language> = diagnosticSourceLanguages

    override fun stopIfSignificantErrorsReceived(messageProvider: () -> String) {
        if (counters[FATAL] != 0 || counters[ERROR] != 0) {
            val message = messageProvider()
            info(message)

            stoppingPolicy.stopProcessing()
        }
    }

    fun setDiagnosticSourceLanguages(sourceLanguages: List<Language>) {
        diagnosticSourceLanguages = sourceLanguages.toCollection(mutableListOf())
    }

    private fun incrementCounter(severity: Severity) {
        val current = counters.getValue(severity)
        counters[severity] = current + 1
    }
}
