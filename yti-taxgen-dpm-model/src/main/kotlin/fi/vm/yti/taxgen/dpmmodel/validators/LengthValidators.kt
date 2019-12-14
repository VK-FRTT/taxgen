package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import kotlin.reflect.KProperty0

fun <T : Any> validatePropsLengths(
    validationResultBuilder: ValidationResultBuilder,
    properties: List<KProperty0<T>>,
    minLength: Int? = null,
    maxLength: Int? = null
) {
    properties.forEach {
        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = it,
            minLength = minLength,
            maxLength = maxLength
        )
    }
}

fun <T : Any> validatePropLength(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<T>,
    minLength: Int? = null,
    maxLength: Int? = null
) {

    if (minLength != null) {
        minLength(validationResultBuilder, property, minLength)
    }

    if (maxLength != null) {
        maxLength(validationResultBuilder, property, maxLength)
    }
}

private fun <T : Any> minLength(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<T>,
    minLength: Int
) {
    val value: T = property.get()

    when (value) {
        is String -> {
            if (value.length < minLength) {
                validationResultBuilder.addError(
                    valueName = property.name.capitalize(),
                    reason = "Too short (minimum $minLength characters)"
                )
            }
        }

        is Collection<*> -> {
            if (value.size < minLength) {
                validationResultBuilder.addError(
                    valueName = property.name.capitalize(),
                    reason = "Too short (minimum $minLength elements)"
                )
            }
        }

        else -> unsupportedTargetDataType(
            "minLength",
            value,
            property
        )
    }
}

private fun <T : Any> maxLength(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<T>,
    maxLength: Int
) {
    val value: T = property.get()

    when (value) {
        is String -> {
            if (value.length > maxLength) {
                validationResultBuilder.addError(
                    valueName = property.name.capitalize(),
                    reason = "Too long (maximum $maxLength characters)"
                )
            }
        }

        is Collection<*> -> {
            if (value.size > maxLength) {
                validationResultBuilder.addError(
                    valueName = property.name.capitalize(),
                    reason = "Too long (maximum $maxLength elements)"
                )
            }
        }

        else -> unsupportedTargetDataType(
            "maxLength",
            value,
            property
        )
    }
}

private fun <T : Any> unsupportedTargetDataType(
    validatorName: String,
    value: T,
    property: KProperty0<T>
) {
    throwIllegalDpmModelState(
        "$validatorName: Unsupported data type ${value::class.simpleName} from property ${property.name}"
    )
}
