package fi.vm.yti.taxgen.datapointmetamodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import kotlin.reflect.KProperty1

fun <T : Any, P : Any> validateLength(
    validationResults: ValidationResults,
    instance: T,
    property: KProperty1<T, P>,
    minLength: Int? = null,
    maxLength: Int? = null
) {

    if (minLength != null) {
        minLength(validationResults, instance, property, minLength)
    }

    if (maxLength != null) {
        maxLength(validationResults, instance, property, maxLength)
    }
}

private fun <T : Any, P : Any> minLength(
    validationResults: ValidationResults,
    instance: T,
    property: KProperty1<T, P>,
    minLength: Int
) {
    val value: P = property.getter.call(instance)

    when (value) {
        is String -> {
            if (value.length < minLength) {
                validationResults.addError(instance, property.name, "is too short (minimum $minLength characters)")
            }
        }

        is Collection<*> -> {
            if (value.size < minLength) {
                validationResults.addError(instance, property.name, "is too short (minimum $minLength elements)")
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
    validationResults: ValidationResults,
    instance: T,
    property: KProperty1<T, P>,
    maxLength: Int
) {
    val value: P = property.getter.call(instance)

    when (value) {
        is String -> {
            if (value.length > maxLength) {
                validationResults.addError(instance, property.name, "is too long (maximum $maxLength characters)")
            }
        }

        is Collection<*> -> {
            if (value.size > maxLength) {
                validationResults.addError(instance, property.name, "is too long (maximum $maxLength elements)")
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
