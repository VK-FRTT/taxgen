package fi.vm.yti.taxgen.dpmmodel.diagnostic

import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidatableInfo

interface Diagnostic {

    fun updateCurrentContextDetails(contextTitle: String? = null, contextIdentifier: String? = null)

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

    fun stopIfSignificantErrorsReceived(messageProvider: () -> String)
}
