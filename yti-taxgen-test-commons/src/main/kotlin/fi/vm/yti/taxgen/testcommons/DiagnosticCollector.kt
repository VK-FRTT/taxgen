package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.datavalidation.ValidationContextInfo
import fi.vm.yti.taxgen.commons.diagostic.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.diagostic.ValidationResultInfo

class DiagnosticCollector : DiagnosticConsumer {
    val events = mutableListOf<String>()

    private fun contextToString(context: ContextInfo): String =
        "CTX{${context.type},${context.label},${context.identifier}}"

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

    override fun topContextDetailsChange(contextStack: List<ContextInfo>, originalContext: ContextInfo) {
        events.add(
            "UPDATE [${contextStack.joinToString { contextToString(it) }}] ORIGINAL [${contextToString(
                originalContext
            )}]"
        )
    }

    override fun message(severity: Severity, message: String) {
        events.add("MESSAGE [$severity] MESSAGE [$message]")
    }

    override fun validationResults(
        validationContextInfo: ValidationContextInfo,
        validationResults: List<ValidationResultInfo>
    ) {
        //TODO - validationContextInfo
        validationResults.forEach {
            events.add("VALIDATION [${it.className.substringAfterLast(".")}.${it.propertyName}: ${it.message}]")
        }
    }
}
