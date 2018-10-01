package fi.vm.yti.taxgen.commons.diagostic

interface Diagnostic {

    fun <R> withContext(
        diagnosticContext: DiagnosticContextProvider,
        block: () -> R
    ): R

    fun <R> withContext(
        contextType: DiagnosticContextType,
        contextName: String = "",
        contextRef: String = "",
        block: () -> R
    ): R

    fun updateCurrentContextName(name: String?)

    fun fatal(message: String): Nothing
    fun error(message: String)
    fun info(message: String)

    fun validationResults(validationResults: List<ValidationResultInfo>)

    fun counters(): Map<Severity, Int>
}
