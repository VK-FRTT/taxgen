package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.diagostic.ContextInfo
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticConsumer
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.diagostic.ValidationResultInfo

class DiagnosticCollector : DiagnosticConsumer {

    val events = mutableListOf<String>()

    var fatalCount = 0
    var errorCount = 0
    var warningCount = 0
    var infoCount = 0
    var validationCount = 0

    override fun contextEnter(contextStack: List<ContextInfo>) {
        events.add("ENTER [${contextStack.firstOrNull()?.type ?: ""}] [${contextStack.firstOrNull()?.label ?: ""}]")
    }

    override fun contextExit(contextStack: List<ContextInfo>, retiredContext: ContextInfo) {
        events.add("EXIT [${retiredContext.type}]")
    }

    override fun topContextDetailsChange(contextStack: List<ContextInfo>, originalContext: ContextInfo) {
        events.add("UPDATE [${originalContext.type}]")
    }

    override fun message(severity: Severity, message: String) {
        events.add("MESSAGE [$severity] [$message]")
        when (severity) {
            Severity.FATAL -> fatalCount++
            Severity.ERROR -> errorCount++
            Severity.WARNING -> warningCount++
            Severity.INFO -> infoCount++
        }
    }

    override fun validationResults(
        validatableInfo: ValidatableInfo,
        validationResults: List<ValidationResultInfo>
    ) {
        //TODO - Event should be: VALIDATION [SubjectType] [SubjectIdentifier] [Input] [Explanation]
        //Input should be: Array<InputName, InputValue>

        events.add("VALIDATED OBJECT [${validatableInfo.objectKind}] [${validatableInfo.objectAddress}]")

        validationResults.forEach {
            events.add("VALIDATION [${it.className.substringAfterLast(".")}.${it.propertyName}: ${it.message}]")
            validationCount++
        }
    }

    fun eventsString(): String {
        return events.joinToString(separator = "\n")
    }

    fun reset() {
        events.clear()

        fatalCount = 0
        errorCount = 0
        warningCount = 0
        infoCount = 0
        validationCount = 0
    }

    fun allMessagesCount() = criticalMessagesCount() + informalMessagesCount() + validationMessagesCount()
    fun criticalMessagesCount() = fatalCount + errorCount
    fun informalMessagesCount() = warningCount + infoCount
    fun validationMessagesCount() = validationCount
}
