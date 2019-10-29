package fi.vm.yti.taxgen.dpmmodel.datavalidation

interface Validatable {

    fun validate(validationResults: ValidationResults)
}
