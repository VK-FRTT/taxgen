package fi.vm.yti.taxgen.dpmmodel.validation

import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor

interface Validatable {
    fun validate(validationResultBuilder: ValidationResultBuilder)
    fun validationSubjectDescriptor(): ValidationSubjectDescriptor
}
