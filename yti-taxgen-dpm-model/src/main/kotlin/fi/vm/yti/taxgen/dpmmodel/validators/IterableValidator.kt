package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import kotlin.reflect.KProperty1

fun <I : Validatable, P : Iterable<E>, E : Any, K : Any> validateIterablePropertyValuesUnique(
    validationResults: ValidationResults,
    instance: I,
    iterableProperty: KProperty1<I, P>,
    valueProperty: KProperty1<E, K>
) {
    val iterable: P = iterableProperty.getter.call(instance)

    validateIterablePropertyValuesUnique(
        validationResults,
        instance,
        iterableProperty.name,
        iterable,
        valueProperty
    )
}

fun <I : Validatable, E : Any, K : Any> validateIterablePropertyValuesUnique(
    validationResults: ValidationResults,
    instance: I,
    instancePropertyName: String,
    iterable: Iterable<E>,
    valueProperty: KProperty1<E, K>
) {
    val duplicateValues = iterable
        .groupingBy { valueProperty.getter.call(it) }
        .eachCount()
        .filter { it.value > 1 }.keys

    duplicateValues.forEach { value ->
        validationResults.addError(
            instance,
            instancePropertyName,
            "duplicate ${valueProperty.name} value '$value'"
        )
    }
}

fun <I : Validatable, E : Any, K : Any> validateIterableElementsUnique(
    validationResults: ValidationResults,
    instance: I,
    propertyName: String,
    iterable: Iterable<E>,
    keySelector: (E) -> K,
    message: (E) -> String
) {
    iterable
        .groupBy(keySelector)
        .filter { (_, elements) -> elements.size > 1 }
        .forEach { (_, elements) ->
            elements.forEach { element ->
                validationResults.addError(instance, propertyName, message(element))
            }
        }
}
