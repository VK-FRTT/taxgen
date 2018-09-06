package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.diagostic.Severity.ERROR
import java.io.PrintWriter

class DiagnosticTextPrinter(
    private val printWriter: PrintWriter
) : DiagnosticConsumer {

    private var level = 0

    override fun contextEnter(contextStack: List<DiagnosticConsumer.ContextInfo>) {
        level = contextStack.size
        val context = contextStack.first()
        printContext(context)
    }

    override fun contextExit(
        contextStack: List<DiagnosticConsumer.ContextInfo>,
        retiredContext: DiagnosticConsumer.ContextInfo
    ) {
        printNestedLine("${retiredContext.type.displayName}: OK")
        level = contextStack.size
    }

    override fun topContextNameChange(
        contextStack: List<DiagnosticConsumer.ContextInfo>,
        originalContext: DiagnosticConsumer.ContextInfo
    ) {
        val context = contextStack.first()
        printContext(context)
    }

    override fun message(severity: Severity, message: String) {
        printNestedLine("  $severity: $message")
    }

    override fun validationErrors(validationErrors: ValidationErrors) {
        validationErrors.errorsInSimpleFormat().forEach {
            message(ERROR, it)
        }
    }

    private fun printContext(context: ContextInfo) {
        val remainder =
            (if (context.name.isNotBlank()) "${context.name} " else "") +
            (if (context.ref.isNotBlank()) "(${context.ref})" else "")

        val line = "${context.type.displayName}" +
            (if (remainder.isNotBlank()) ": $remainder" else "")

        printNestedLine(line)
    }

    private fun printNestedLine(message: String) {
        printWriter.println("  ".repeat(level) + message)
    }
}
