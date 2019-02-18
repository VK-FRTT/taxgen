package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.diagostic.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.diagostic.ValidationResultInfo
import java.io.PrintWriter

class DiagnosticTextPrinter(
    private val printWriter: PrintWriter
) : DiagnosticConsumer {

    private var level = 0
    private var lineHeader: String = ""

    override fun contextEnter(contextStack: List<ContextInfo>) {
        level = levelFromStack(contextStack)

        val context = contextStack.first()
        lineHeader = context.contextHeader()
        printLine(context.contextDetails())
    }

    override fun contextExit(
        contextStack: List<ContextInfo>,
        retiredContext: ContextInfo
    ) {
        printLine("OK")

        lineHeader = contextStack.firstOrNull()?.contextHeader() ?: ""
        level = levelFromStack(contextStack)
    }

    override fun topContextDetailsChange(
        contextStack: List<ContextInfo>,
        originalContext: ContextInfo
    ) {
        printLine(contextStack.first().contextDetailsForUpdate(originalContext))
    }

    override fun message(severity: Severity, message: String) {
        printLine("$severity: $message")
    }

    override fun validationResults(
        validatableInfo: ValidatableInfo,
        validationResults: List<ValidationResultInfo>
    ) {
        validationResults.forEach {
            message(Severity.ERROR, "${validatableInfo.objectKind} (${validatableInfo.objectAddress}) => ${it.propertyName}: ${it.message}")
        }
    }

    private fun levelFromStack(contextStack: List<ContextInfo>) = (contextStack.size - 1).coerceAtLeast(0)

    private fun printLine(message: String) {
        printWriter.println("${"   ".repeat(level)}$lineHeader $message")
    }

    private fun ContextInfo.contextHeader(): String {
        return if (type.recurring) {
            "${type.displayName} #$recurrenceIndex:"
        } else {
            "${type.displayName}:"
        }
    }

    private fun ContextInfo.contextDetails(): String {
        val separatorValue = " "
        var separator = ""
        var details = ""

        if (label.isNotBlank()) {
            separator = separatorValue
            details += "$label"
        }

        if (identifier.isNotBlank()) {
            details += separator
            details += "($identifier)"
        }

        return details
    }

    private fun ContextInfo.contextDetailsForUpdate(original: ContextInfo): String {
        val separatorValue = " "
        var separator = ""
        var details = ""

        if (label.isNotBlank() && label != original.label) {
            separator = separatorValue
            details += "$label"
        }

        if (identifier.isNotBlank() && identifier != original.identifier) {
            details += separator
            details += "($identifier)"
        }

        return details
    }
}
