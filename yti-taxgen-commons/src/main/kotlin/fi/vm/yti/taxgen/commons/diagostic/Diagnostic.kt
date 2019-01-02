package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.Validatable

interface Diagnostic {

    fun <R> withContext(
        diagnosticContext: DiagnosticContextProvider,
        block: () -> R
    ): R

    fun <R> withContext(
        contextType: DiagnosticContextType,
        contextLabel: String = "",
        contextIdentifier: String = "",
        block: () -> R
    ): R

    fun updateCurrentContextDetails(label: String? = null, identifier: String? = null)

    fun fatal(message: String): Nothing
    fun error(message: String)
    fun info(message: String)

    fun validate(validatable: Validatable)
    fun validate(validatables: List<Validatable>)

    fun counters(): Map<Severity, Int>
}
