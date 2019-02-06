package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import java.time.Instant
import kotlin.reflect.KProperty1

@Suppress("FINAL_UPPER_BOUND")
fun <I : Any, P : Instant> validateTimestamp(
    validationResults: ValidationResults,
    instance: I,
    property: KProperty1<I, P>
) {
    val instant: P = property.getter.call(instance)

    if (instant == Instant.EPOCH) {
        validationResults.addError(
            instance = instance,
            propertyName = property.name,
            message = "is illegal timestamp value"
        )
    }
}
