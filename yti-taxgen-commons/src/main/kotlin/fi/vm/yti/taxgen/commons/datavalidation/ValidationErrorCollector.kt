package fi.vm.yti.taxgen.commons.datavalidation

import kotlin.reflect.KProperty1

class ValidationErrorCollector : ValidationErrors {

    private val errors: MutableMap<String, MutableMap<String, MutableList<String>>> = mutableMapOf()

    override fun <T : Any, P : Any?> add(instance: T, property: KProperty1<T, P>, message: String) {
        val className = instance.javaClass.canonicalName
        val propertyName = property.name

        errors.getOrPut(className) { mutableMapOf() }.getOrPut(propertyName) { mutableListOf() }.add(message)
    }

    override fun errorsInSimpleFormat(): List<String> {
        return errors.flatMap { (className, propertyErrors) ->
            propertyErrors.flatMap { (propertyName, messages) ->
                messages.map { message ->
                    "${className.substringAfterLast(".")}.$propertyName: $message"
                }
            }
        }.sorted()
    }

    fun any(): Boolean = errors.any()
}
