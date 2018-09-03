package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.Severity

class DiagnosticConsumerCaptor : DiagnosticConsumer {
    val events = mutableListOf<String>()

    private fun contextToString(context: ContextInfo): String = "CTX{${context.type},${context.name},${context.ref}}"

    override fun contextEnter(contextStack: List<ContextInfo>) {
        events.add("ENTER [${contextStack.joinToString { contextToString(it) }}]")
    }

    override fun contextExit(contextStack: List<ContextInfo>, retiredContext: ContextInfo) {
        events.add(
            "EXIT [${contextStack.joinToString { contextToString(it) }}] RETIRED [${contextToString(
                retiredContext
            )}]"
        )
    }

    override fun topContextNameChange(contextStack: List<ContextInfo>, originalContext: ContextInfo) {
        events.add(
            "UPDATE [${contextStack.joinToString { contextToString(it) }}] ORIGINAL [${contextToString(
                originalContext
            )}]"
        )
    }

    override fun message(severity: Severity, message: String) {
        events.add("MESSAGE [$severity] MESSAGE [$message]")
    }

    override fun validationErrors(validationErrors: ValidationErrors) {
        events.add("VALIDATION [${validationErrors.errorsInSimpleFormat()}]")
    }
}
