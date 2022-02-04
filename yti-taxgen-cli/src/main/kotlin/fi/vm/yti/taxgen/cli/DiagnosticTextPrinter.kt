package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticContextDescriptor
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticEventConsumer
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationResultDescriptor
import java.io.PrintWriter

class DiagnosticTextPrinter(
    private val printWriter: PrintWriter
) : DiagnosticEventConsumer {

    private var currentIndentationLevel = 0
    private var lineHeader: String = ""

    override fun contextEnter(contextStack: List<DiagnosticContextDescriptor>) {
        currentIndentationLevel = levelFromStack(contextStack)

        val context = contextStack.first()
        lineHeader = context.contextHeader()
        printLineWithHeader(context.contextDetails())
    }

    override fun contextExit(
        contextStack: List<DiagnosticContextDescriptor>,
        retiredContext: DiagnosticContextDescriptor
    ) {
        printLineWithHeader("OK")

        lineHeader = contextStack.firstOrNull()?.contextHeader() ?: ""
        currentIndentationLevel = levelFromStack(contextStack)
    }

    override fun topContextDetailsChange(
        contextStack: List<DiagnosticContextDescriptor>,
        originalContext: DiagnosticContextDescriptor
    ) {
        printLineWithHeader(contextStack.first().contextDetailsForUpdate(originalContext))
    }

    override fun message(severity: Severity, message: String) {
        printLineWithIndentation(currentIndentationLevel + 1, "$severity: $message")
    }

    override fun validationResults(results: List<ValidationResultDescriptor>) {
        results.forEach { result ->

            val sb = StringBuilder()
            sb.append("${result.reason()}")

            if (result.hasValue()) {
                sb.append(" '${result.value()}'")
            }

            sb.append(" in ${result.valueName()}")

            result.subjectChain().reversed().forEach {
                sb.append(" in ${it.subjectType} (${it.subjectIdentifiers.joinToString() })")
            }

            message(Severity.ERROR, sb.toString())
        }
    }

    private fun levelFromStack(contextStack: List<DiagnosticContextDescriptor>) =
        (contextStack.size - 1).coerceAtLeast(0)

    private fun printLineWithHeader(message: String) {
        printLineWithIndentation(currentIndentationLevel, "$lineHeader $message")
    }

    private fun printLineWithIndentation(indentationLevel: Int, message: String) {
        printWriter.println("${"   ".repeat(indentationLevel)}$message")
    }

    private fun DiagnosticContextDescriptor.contextHeader(): String {
        val contextTypeEnum = DiagnosticContexts.valueOf(contextType.typeName)

        return if (this.contextType.recurringContext) {
            "${contextTypeEnum.displayName} #$recurrenceIndex:"
        } else {
            "${contextTypeEnum.displayName}:"
        }
    }

    private fun DiagnosticContextDescriptor.contextDetails(): String {
        val separatorValue = " "
        var separator = ""
        var details = ""

        if (contextTitle.isNotBlank()) {
            separator = separatorValue
            details += "$contextTitle"
        }

        if (contextIdentifier.isNotBlank()) {
            details += separator
            details += "($contextIdentifier)"
        }

        return details
    }

    private fun DiagnosticContextDescriptor.contextDetailsForUpdate(original: DiagnosticContextDescriptor): String {
        val separatorValue = " "
        var separator = ""
        var details = ""

        if (contextTitle.isNotBlank() && contextTitle != original.contextTitle) {
            separator = separatorValue
            details += "$contextTitle"
        }

        if (contextIdentifier.isNotBlank() && contextIdentifier != original.contextIdentifier) {
            details += separator
            details += "($contextIdentifier)"
        }

        return details
    }
}
