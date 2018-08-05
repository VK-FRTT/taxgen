package fi.vm.yti.taxgen.commons.datavalidation

import kotlin.reflect.KProperty1

fun <T : Validatable, P : Any> validateLength(
    validationErrors: ValidationErrors,
    instance: T,
    property: KProperty1<T, P>,
    minLength: Int? = null,
    maxLength: Int? = null
) {

    if (minLength != null) {
        minLength(validationErrors, instance, property, minLength)
    }

    if (maxLength != null) {
        maxLength(validationErrors, instance, property, maxLength)
    }
}

private fun <T : Validatable, P : Any> minLength(
    validationErrors: ValidationErrors,
    instance: T,
    property: KProperty1<T, P>,
    minLength: Int
) {
    val value: P = property.getter.call(instance)

    when (value) {
        is String -> {
            if (value.length < minLength) {
                validationErrors.add(instance, property, "is too short (minimum $minLength characters)")
            }
        }

        is Collection<*> -> {
            if (value.size < minLength) {
                validationErrors.add(instance, property, "is too short (minimum $minLength elements)")
            }
        }

        else -> unsupportedValidationDataType("minLength", value, instance, property)
    }
}

private fun <T : Validatable, P : Any> maxLength(
    validationErrors: ValidationErrors,
    instance: T,
    property: KProperty1<T, P>,
    maxLength: Int
) {
    val value: P = property.getter.call(instance)

    when (value) {
        is String -> {
            if (value.length > maxLength) {
                validationErrors.add(instance, property, "is too long (maximum $maxLength characters)")
            }
        }

        is Collection<*> -> {
            if (value.size > maxLength) {
                validationErrors.add(instance, property, "is too long (maximum $maxLength elements)")
            }
        }

        else -> unsupportedValidationDataType("maxLength", value, instance, property)
    }
}
