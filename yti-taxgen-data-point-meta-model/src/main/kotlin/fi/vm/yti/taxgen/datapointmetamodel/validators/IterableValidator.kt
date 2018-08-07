package fi.vm.yti.taxgen.datapointmetamodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import kotlin.reflect.KProperty1

fun <I : Validatable, P : Iterable<E>, E : Any, K : Any> validateIterableElementsUnique(
    validationErrors: ValidationErrors,
    instance: I,
    property: KProperty1<I, P>,
    keySelector: (E) -> K
) {
    val iterable: P = property.getter.call(instance)
    val duplicates = iterable.groupingBy(keySelector).eachCount().filter { it.value > 1 }.keys

    if (duplicates.any()) {
        validationErrors.add(instance, property, "contains duplicate elements $duplicates")
    }
}
