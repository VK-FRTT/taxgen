package fi.vm.yti.taxgen.dpmmodel.datavalidation

interface ValidationResults {

    fun <T : Any> addError(instance: T, propertyName: String, message: String)
}
