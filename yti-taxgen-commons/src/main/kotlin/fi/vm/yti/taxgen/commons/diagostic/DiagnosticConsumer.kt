package fi.vm.yti.taxgen.commons.diagostic

interface DiagnosticConsumer {

    fun contextEnter(contextStack: List<ContextInfo>)
    fun contextExit(contextStack: List<ContextInfo>, retiredContext: ContextInfo)
    fun topContextDetailsChange(contextStack: List<ContextInfo>, originalContext: ContextInfo)

    fun message(severity: Severity, message: String)
    fun validationResults(validationResults: List<ValidationResultInfo>)
}
