package fi.vm.yti.taxgen.datapointmetamodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.datapointmetamodel.DpmElementRef
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
        condition = { ref.id.isNotEmpty() && ref.id.isNotBlank() },
        message = { "empty or blank id" }
    )
}
