package fi.vm.yti.taxgen.commons.datavalidation

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import kotlin.reflect.KProperty1

internal fun <T : Any, P : Any> unsupportedValidationDataType(
    validatorName: String,
    value: P,
    instance: T,
    property: KProperty1<T, P>
) {
    thisShouldNeverHappen(
        "$validatorName: Unsupported data type ${value::class.simpleName} for ${instance.javaClass.simpleName}::${property.name}"
    )
}
