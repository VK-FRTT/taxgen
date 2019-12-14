package fi.vm.yti.taxgen.dpmmodel.validation

import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import kotlin.reflect.KClass

interface ValidationResultBuilder {

    fun addError(
        valueName: Any,
        reason: String
    )

    fun addError(
        valueName: Any,
        reason: String,
        value: String
    )

    fun addError(
        subject: ValidationSubjectDescriptor,
        valueName: Any,
        reason: String,
        value: String
    )

    fun withSubject(subject: ValidationSubjectDescriptor, block: () -> Unit)
    fun withNestedObject(objectClass: KClass<*>, block: () -> Unit)
}
