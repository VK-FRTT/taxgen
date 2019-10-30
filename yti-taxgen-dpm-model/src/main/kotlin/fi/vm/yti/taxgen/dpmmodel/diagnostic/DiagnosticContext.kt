package fi.vm.yti.taxgen.dpmmodel.diagnostic

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidatableInfo

interface DiagnosticContext : Diagnostic {

    fun <R> withContext(
        contextType: DiagnosticContextType,
        contextDetails: DiagnosticContextDetails?,
        action: () -> R
    ): R

    override fun updateCurrentContextDetails(contextTitle: String?, contextIdentifier: String?)

    override fun fatal(message: String): Nothing
    override fun error(message: String)
    override fun info(message: String)

    override fun validate(
        validatable: Validatable
    )

    override fun validate(
        validatable: Validatable,
        infoProvider: () -> ValidatableInfo
    )

    override fun diagnosticSourceLanguages(): List<Language>

    override fun stopIfSignificantErrorsReceived(messageProvider: () -> String)
}
