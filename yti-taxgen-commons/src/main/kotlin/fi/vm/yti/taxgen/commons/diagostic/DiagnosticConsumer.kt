package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.ValidationContextInfo

interface DiagnosticConsumer {

    fun contextEnter(contextStack: List<ContextInfo>)
    fun contextExit(contextStack: List<ContextInfo>, retiredContext: ContextInfo)
    fun topContextDetailsChange(contextStack: List<ContextInfo>, originalContext: ContextInfo)

    fun message(severity: Severity, message: String)
    fun validationResults(
        validationContextInfo: ValidationContextInfo,
        validationResults: List<ValidationResultInfo>
    )
}
