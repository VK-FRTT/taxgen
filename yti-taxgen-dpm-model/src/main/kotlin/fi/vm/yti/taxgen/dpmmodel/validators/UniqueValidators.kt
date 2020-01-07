package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder

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
