package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import kotlin.reflect.KProperty0

fun <T : Any?> validateNonNull(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<T>,
    reasonDetail: String? = null
) {
    val value: T = property.get()

    if (value == null)
        validationResultBuilder.addError(
            valueName = property,
            reason = "Value missing${if (reasonDetail == null) "" else " ($reasonDetail)"}"
        )
}

fun validatePropNullOrNonBlank(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<String?>
) {
    val value: String? = property.get()

    if (value != null) {
        if (value.isBlank()) {
            validationResultBuilder.addError(
                valueName = property,
                reason = "Value is blank"
            )
        }
    }
}

fun validateNonNullAndNonBlank(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<String?>
) {
    validateNonNull(
        validationResultBuilder,
        property
    )

    validatePropNullOrNonBlank(
        validationResultBuilder,
        property
    )
}
