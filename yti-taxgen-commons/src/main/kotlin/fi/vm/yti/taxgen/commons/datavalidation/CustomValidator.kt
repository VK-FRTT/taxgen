package fi.vm.yti.taxgen.commons.datavalidation

import kotlin.reflect.KProperty1

fun <I : Validatable, P : Any?> customValidate(
    validationErrors: ValidationErrors,
    instance: I,
    property: KProperty1<I, P>,
    failIf: (MutableMap<String, Any>) -> (Boolean),
    failMsg: (Map<String, Any>) -> (String)
) {
    val notes = mutableMapOf<String, Any>()

    if (failIf(notes)) {
        validationErrors.add(
            instance = instance,
            property = property,
            message = failMsg(notes)
        )
    }
}




