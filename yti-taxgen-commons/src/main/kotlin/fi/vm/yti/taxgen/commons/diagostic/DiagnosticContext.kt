package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo

interface DiagnosticContext : Diagnostic {

    fun <R> withContext(
        contextType: DiagnosticContextType,
        contextDetails: DiagnosticContextDetails?,
        action: () -> R
    ): R {
        return withContext(
            contextType = contextType,
            contextLabel = contextDetails?.contextLabel() ?: "",
            contextIdentifier = contextDetails?.contextIdentifier() ?: "",
            action = action
        )
    }

    fun <R> withContext(
        contextType: DiagnosticContextType,
        contextLabel: String = "",
        contextIdentifier: String = "",
        action: () -> R
    ): R

    override fun updateCurrentContextDetails(label: String?, identifier: String?)

    override fun fatal(message: String): Nothing
    override fun error(message: String)
    override fun info(message: String)

    override fun validate(
        validatable: Validatable,
        validatableInfo: ValidatableInfo?
    )

    override fun counters(): Map<Severity, Int>
}
