package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors

interface DiagnosticConsumer {

    data class ContextInfo(
        val type: DiagnosticContextType,
        val name: String,
        val ref: String
    )

    fun contextEnter(contextStack: List<ContextInfo>)
    fun contextExit(contextStack: List<ContextInfo>, retiredContext: ContextInfo)
    fun topContextNameChange(contextStack: List<ContextInfo>, originalContext: ContextInfo)

    fun message(severity: Severity, message: String)
    fun validationErrors(validationErrors: ValidationErrors)
}
