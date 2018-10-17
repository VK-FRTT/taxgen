package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.diagostic.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.diagostic.ValidationResultInfo

class DiagnosticCollectorSimple : DiagnosticConsumer {

    val events = mutableListOf<String>()

    override fun contextEnter(contextStack: List<ContextInfo>) {
        events.add("ENTER [${contextStack.firstOrNull()?.type ?: ""}]")
    }

    override fun contextExit(contextStack: List<ContextInfo>, retiredContext: ContextInfo) {
        events.add("EXIT [${retiredContext.type}]")
    }

    override fun topContextDetailsChange(contextStack: List<ContextInfo>, originalContext: ContextInfo) {
        events.add("UPDATE [${originalContext.type}]")
    }

    override fun message(severity: Severity, message: String) {
        events.add("MESSAGE [$severity] MESSAGE [$message]")
    }

    override fun validationResults(validationResults: List<ValidationResultInfo>) {
        validationResults.forEach {
            events.add("VALIDATION [${it.className.substringAfterLast(".")}.${it.propertyName}: ${it.message}]")
        }
    }
}
