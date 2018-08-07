package fi.vm.yti.taxgen.datapointmetamodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import kotlin.reflect.KProperty1

fun <T : Any, P : Any> validateLength(
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

private fun <T : Any, P : Any> minLength(
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

        else -> unsupportedTargetDataType(
            "minLength",
            value,
            instance,
            property
        )
    }
}

private fun <T : Any, P : Any> maxLength(
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

        else -> unsupportedTargetDataType(
            "maxLength",
            value,
            instance,
            property
        )
    }
}

private fun <T : Any, P : Any> unsupportedTargetDataType(
    validatorName: String,
    value: P,
    instance: T,
    property: KProperty1<T, P>
) {
    thisShouldNeverHappen(
        "$validatorName: Unsupported data type ${value::class.simpleName} for ${instance.javaClass.simpleName}::${property.name}"
    )
}
