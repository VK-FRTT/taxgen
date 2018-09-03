package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.Severity

class DiagnosticConsumerCaptorSimple : DiagnosticConsumer {

    val events = mutableListOf<String>()

    override fun contextEnter(contextStack: List<ContextInfo>) {
        events.add("ENTER [${contextStack.firstOrNull()?.type ?: ""}]")
    }

    override fun contextExit(contextStack: List<ContextInfo>, retiredContext: ContextInfo) {
        events.add("EXIT [${contextStack.firstOrNull()?.type ?: ""}] RETIRED [${retiredContext.type}]")
    }

    override fun topContextNameChange(contextStack: List<ContextInfo>, originalContext: ContextInfo) {
        events.add("UPDATE [${contextStack.firstOrNull()?.type ?: ""}] ORIGINAL [${originalContext.type}]")
    }

    override fun message(severity: Severity, message: String) {
        events.add("MESSAGE [$severity] MESSAGE [$message]")
    }

    override fun validationErrors(validationErrors: ValidationErrors) {
        events.add("VALIDATION [${validationErrors.errorsInSimpleFormat()}]")
    }
}
