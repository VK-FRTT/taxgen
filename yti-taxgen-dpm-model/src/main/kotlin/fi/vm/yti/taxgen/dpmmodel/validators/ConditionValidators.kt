package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationErrorReporter
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationErrorRelay
import kotlin.reflect.KProperty0

fun <T : Any?> validatePropFulfillsCondition(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<T>,
    condition: (T) -> (Boolean),
    reason: () -> (String),
    includeValueToError: Boolean = true
) {
    val value: T = property.get()

    if (!condition(value)) {

        if (includeValueToError) {
            validationResultBuilder.addError(
                valueName = property,
                reason = reason(),
                value = value.toString()
            )
        } else {
            validationResultBuilder.addError(
                valueName = property,
                reason = reason()
            )
        }
    }
}

fun validateCustom(
    validationResultBuilder: ValidationResultBuilder,
    valueName: Any,
    validate: (ValidationErrorReporter) -> Unit
) {
    val relay = ValidationErrorRelay(
        validationResultBuilder,
        valueName
    )

    validate(relay)
}
