package fi.vm.yti.taxgen.datapointmetamodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import kotlin.reflect.KProperty1

fun <I : Validatable, P : Iterable<E>, E : Any, K : Any> validateIterableKeysUnique(
    validationErrors: ValidationErrors,
    instance: I,
    iterableProperty: KProperty1<I, P>,
    keyProperty: KProperty1<E, K>
) {
    val iterable: P = iterableProperty.getter.call(instance)
    val duplicates = findDuplicates(iterable, { keyProperty.getter.call(it) })

    if (duplicates.any()) {
        validationErrors.add(instance, iterableProperty.name, "${keyProperty.name} has duplicate values $duplicates")
    }
}

fun <I : Validatable, E : Any, K : Any> validateIterableKeysUnique(
    validationErrors: ValidationErrors,
    instance: I,
    iterable: Iterable<E>,
    iterablePropertyName: String,
    keySelector: (E) -> K,
    errorMessageBuilder: (Set<K>) -> String
) {
    val duplicates = findDuplicates(iterable, keySelector)

    if (duplicates.any()) {
        val message = errorMessageBuilder(duplicates)
        validationErrors.add(instance, iterablePropertyName, message)
    }
}

private fun <E : Any, K : Any> findDuplicates(
    iterable: Iterable<E>,
    keySelector: (E) -> K
): Set<K> = iterable
    .groupingBy(keySelector)
    .eachCount()
    .filter { it.value > 1 }.keys
