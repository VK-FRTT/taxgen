package fi.vm.yti.taxgen.commons.datavalidation

interface ValidationResults {

    fun <T : Any> addError(instance: T, propertyName: String, message: String)
}
