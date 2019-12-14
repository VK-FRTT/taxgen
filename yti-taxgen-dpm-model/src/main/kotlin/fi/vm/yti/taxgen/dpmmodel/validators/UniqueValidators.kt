package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun <E : Any> validateIterableValuesUnique(
    validationResultBuilder: ValidationResultBuilder,
    iterable: Iterable<E>,
    valueSelector: (E) -> String,
    valueName: Any
) {
    val duplicateValues: Set<String> = iterable
        .groupingBy(valueSelector)
        .eachCount()
        .filter { it.value > 1 }.keys

    duplicateValues.forEach { value ->
        validationResultBuilder.addError(
            valueName = valueName,
            reason = "Duplicate value",
            value = value
        )
    }
}

fun <T : Iterable<E>, E : Any, K : Any> validatePropElementPropertiesUnique(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<T>,
    elementProperties: List<KProperty1<E, K>>
) {
    val iterable: T = property.get()

    elementProperties.forEach { elementProperty ->
        validateIterableValuesUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = iterable,
            valueSelector = { elementProperty.getter.call(it).toString() },
            valueName = listOf(property, elementProperty)
        )
    }
}
