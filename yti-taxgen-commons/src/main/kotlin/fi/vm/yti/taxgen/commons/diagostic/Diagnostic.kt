package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationContextInfo

interface Diagnostic {

    fun updateCurrentContextDetails(label: String? = null, identifier: String? = null)

    fun fatal(message: String): Nothing
    fun error(message: String)
    fun info(message: String)

    fun validate(
        validatable: Validatable,
        validationContextInfo: ValidationContextInfo? = null
    )

    fun counters(): Map<Severity, Int>
}
