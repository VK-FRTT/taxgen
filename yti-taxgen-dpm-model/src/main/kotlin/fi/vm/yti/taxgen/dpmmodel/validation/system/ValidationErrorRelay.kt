package fi.vm.yti.taxgen.dpmmodel.validation.system

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationErrorReporter
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder

class ValidationErrorRelay(
    val validationResultBuilder: ValidationResultBuilder,
    val valueName: Any
) : ValidationErrorReporter {

    override fun error(
        reason: String,
        value: String
    ) {
        validationResultBuilder.addError(
            valueName = valueName,
            reason = reason,
            value = value
        )
    }
}
