package fi.vm.yti.taxgen.commons.datavalidation

import kotlin.reflect.KProperty1

fun <I : Validatable, P : Any?> validateCondition(
    validationErrors: ValidationErrors,
    instance: I,
    property: KProperty1<I, P>,
    condition: () -> (Boolean),
    failMessage: () -> (String)
) {
    if (condition()) {
        validationErrors.add(
            instance = instance,
            propertyName = property.name,
            message = failMessage()
        )
    }
}

fun <I : Validatable> validateCustom(
    validationErrors: ValidationErrors,
    instance: I,
    propertyName: String,
    validate: (MutableList<String>) -> Unit
) {
    val messages = mutableListOf<String>()

    validate(messages)

    messages.forEach { message ->
        validationErrors.add(
            instance = instance,
            propertyName = propertyName,
            message = message
        )
    }
}
