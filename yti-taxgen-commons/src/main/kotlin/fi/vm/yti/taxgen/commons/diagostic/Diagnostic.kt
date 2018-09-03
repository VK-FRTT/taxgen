package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors

interface Diagnostic {

    fun <R> withContext(
        diagnosticContext: DiagnosticContextProvider,
        block: () -> R
    ): R

    fun <R> withContext(
        contextType: String,
        contextName: String = "",
        contextRef: String = "",
        block: () -> R
    ): R

    fun updateCurrentContextName(name: String?)

    fun fatal(message: String): Nothing
    fun error(message: String)
    fun info(message: String)
    fun validationErrors(validationErrors: ValidationErrors)

    fun counters(): Map<Severity, Int>
}
