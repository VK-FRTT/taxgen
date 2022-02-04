package fi.vm.yti.taxgen.dpmmodel.validation

interface ValidatableNestedObject {
    fun validate(validationResultBuilder: ValidationResultBuilder)
}
