package fi.vm.yti.taxgen.commons.datavalidation

interface Validatable {

    fun validate(validationErrors: ValidationErrors)
}
