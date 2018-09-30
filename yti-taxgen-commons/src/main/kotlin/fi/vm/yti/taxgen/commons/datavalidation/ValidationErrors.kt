package fi.vm.yti.taxgen.commons.datavalidation

interface ValidationErrors {

    fun <T : Any> add(instance: T, propertyName: String, message: String)

    fun errorsInSimpleFormat(): List<String> // TODO - perform formatting in printer
}
