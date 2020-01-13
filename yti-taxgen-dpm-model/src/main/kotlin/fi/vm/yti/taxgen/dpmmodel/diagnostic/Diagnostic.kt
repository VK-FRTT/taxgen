package fi.vm.yti.taxgen.dpmmodel.diagnostic

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable

interface Diagnostic {

    fun updateCurrentContextDetails(contextTitle: String? = null, contextIdentifier: String? = null)

    fun fatal(message: String): Nothing
    fun error(message: String)
    fun warning(message: String)
    fun info(message: String)
    fun debug(message: String)

    fun validate(
        validatable: Validatable
    )

    fun validate(
        validatables: List<Validatable>
    )

    fun diagnosticSourceLanguages(): List<Language>
}
