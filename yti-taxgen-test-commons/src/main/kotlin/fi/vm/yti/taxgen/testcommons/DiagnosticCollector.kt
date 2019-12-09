package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.dpmmodel.datavalidation.system.ValidationResultInfo
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticContextDescriptor
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticEventConsumer
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity

class DiagnosticCollector : DiagnosticEventConsumer {

    val events = mutableListOf<String>()

    private var fatalCount = 0
    private var errorCount = 0
    private var warningCount = 0
    private var infoCount = 0
    private var objectValidationFailureCount = 0

    override fun contextEnter(contextStack: List<DiagnosticContextDescriptor>) {
        val topContext = contextStack.firstOrNull()

        events.add(
            "ENTER [${topContext?.contextType?.typeName ?: ""}] [${topContext?.contextTitle ?: ""}]"
        )
    }

    override fun contextExit(
        contextStack: List<DiagnosticContextDescriptor>,
        retiredContext: DiagnosticContextDescriptor
    ) {
        events.add("EXIT [${retiredContext.contextType.typeName}]")
    }

    override fun topContextDetailsChange(
        contextStack: List<DiagnosticContextDescriptor>,
        originalContext: DiagnosticContextDescriptor
    ) {
        val topContext = contextStack.firstOrNull()

        events.add("UPDATE [${originalContext.contextType.typeName}] [${topContext?.contextTitle ?: ""}]")
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
        // TODO - Event should be: VALIDATION [SubjectType] [SubjectIdentifier] [Input] [Explanation]
        // Input should be: Array<InputName, InputValue>

        events.add("VALIDATED OBJECT [${validatableInfo.objectKind}] [${validatableInfo.objectAddress}]")
        objectValidationFailureCount++

        validationResults.forEach {
            events.add("VALIDATION [${it.className.substringAfterLast(".")}.${it.propertyName}: ${it.message}]")
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
        objectValidationFailureCount = 0
    }

    fun allMessagesCount() = criticalMessagesCount() + informalMessagesCount() + objectValidationFailureCount()
    fun criticalMessagesCount() = fatalCount + errorCount
    fun informalMessagesCount() = warningCount + infoCount
    fun objectValidationFailureCount() = objectValidationFailureCount
}
