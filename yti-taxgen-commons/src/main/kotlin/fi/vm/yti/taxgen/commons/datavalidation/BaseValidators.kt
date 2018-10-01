package fi.vm.yti.taxgen.commons.datavalidation

import kotlin.reflect.KProperty1

fun <I : Validatable, P : Any?> validateConditionTruthy(
    validationResults: ValidationResults,
    instance: I,
    property: KProperty1<I, P>,
    condition: () -> (Boolean),
    message: () -> (String)
) {
    if (!condition()) {
        validationResults.addError(
            instance = instance,
            propertyName = property.name,
            message = message()
        )
    }
}

fun <I : Validatable> validateCustom(
    validationResults: ValidationResults,
    instance: I,
    propertyName: String,
    validate: (MutableList<String>) -> Unit
) {
    val messages = mutableListOf<String>()

    validate(messages)

    messages.forEach { message ->
        validationResults.addError(
            instance = instance,
            propertyName = propertyName,
            message = message
        )
    }
}
