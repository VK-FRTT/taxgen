package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import java.time.Instant
import kotlin.reflect.KProperty0

fun validatePropTimestamp(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<Instant>
) {
    val instant = property.get()

    if (instant == Instant.EPOCH) {
        validationResultBuilder.addError(
            valueName = property,
            reason = "Illegal timestamp value",
            value = instant.toString()
        )
    }
}
