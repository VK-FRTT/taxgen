package fi.vm.yti.taxgen.dpmmodel.datavalidation

import kotlin.reflect.KProperty1

fun <I : Any, P : Iterable<E>, E : Any, K : Any> validateElementPropertyValuesUnique(
    validationResults: ValidationResults,
    instance: I,
    iterableProperty: KProperty1<I, P>,
    valueProperties: List<KProperty1<E, K>>
) {
    val iterable: P = iterableProperty.getter.call(instance)

    valueProperties.forEach { valueProperty ->
        validateElementValueUnique(
            validationResults = validationResults,
            instance = instance,
            instancePropertyName = iterableProperty.name,
            iterable = iterable,
            valueSelector = { valueProperty.getter.call(it) },
            valueDescription = valueProperty.name
        )
    }
}

fun <I : Any, E : Any, K : Any> validateElementValueUnique(
    validationResults: ValidationResults,
    instance: I,
    instancePropertyName: String,
    iterable: Iterable<E>,
    valueSelector: (E) -> K,
    valueDescription: String
) {
    val duplicateValues = iterable
        .groupingBy(valueSelector)
        .eachCount()
        .filter { it.value > 1 }.keys

    duplicateValues.forEach { value ->
        validationResults.addError(
            instance,
            instancePropertyName,
            "duplicate $valueDescription value '$value'"
        )
    }
}
