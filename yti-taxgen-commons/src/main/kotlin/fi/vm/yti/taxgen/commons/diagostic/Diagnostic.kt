package fi.vm.yti.taxgen.commons.diagostic

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo

interface Diagnostic {

    fun updateCurrentContextDetails(label: String? = null, identifier: String? = null)

    fun fatal(message: String): Nothing
    fun error(message: String)
    fun warning(message: String)
    fun info(message: String)

    fun validate(
        validatable: Validatable
    )

    fun validate(
        validatable: Validatable,
        infoProvider: () -> ValidatableInfo
    )

    fun haltIfUnrecoverableErrors(messageProvider: () -> String)
}
