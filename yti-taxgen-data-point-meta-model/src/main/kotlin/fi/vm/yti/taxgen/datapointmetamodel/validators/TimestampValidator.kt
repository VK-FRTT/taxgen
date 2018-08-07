package fi.vm.yti.taxgen.datapointmetamodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import java.time.Instant
import kotlin.reflect.KProperty1

@Suppress("FINAL_UPPER_BOUND")
fun <I : Validatable, P : Instant> validateInstantLegalTimestamp(
    validationErrors: ValidationErrors,
    instance: I,
    property: KProperty1<I, P>
) {
    val instant: P = property.getter.call(instance)

    if (instant == Instant.EPOCH) {
        validationErrors.add(
            instance = instance,
            property = property,
            message = "is illegal timestamp value"
        )
    }
}
