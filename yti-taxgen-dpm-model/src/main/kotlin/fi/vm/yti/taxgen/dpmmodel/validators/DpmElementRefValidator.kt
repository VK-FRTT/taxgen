package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.DpmElementRef
import kotlin.reflect.KProperty1

fun <I : Validatable> validateDpmElementRef(
    validationResults: ValidationResults,
    instance: I,
    property: KProperty1<I, DpmElementRef>
) {
    val ref = property.get(instance)

    validateConditionTruthy(
        validationResults = validationResults,
        instance = instance,
        property = property,
        condition = { ref.uri.isNotEmpty() && ref.uri.isNotBlank() },
        message = { "empty or blank uri" }
    )
}

@Suppress("UNCHECKED_CAST")
fun <I : Validatable> validateOptionalDpmElementRef(
    validationResults: ValidationResults,
    instance: I,
    property: KProperty1<I, DpmElementRef?>
) {
    val ref = property.get(instance)

    if (ref != null) {
        validateDpmElementRef(
            validationResults = validationResults,
            instance = instance,
            property = property as KProperty1<I, DpmElementRef>
        )
    }
}
