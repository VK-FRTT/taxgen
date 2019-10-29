package fi.vm.yti.taxgen.dpmmodel.datavalidation.system

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults

class ValidationCollector : ValidationResults {

    private val results: MutableMap<String, MutableMap<String, MutableList<String>>> = mutableMapOf()

    override fun <T : Any> addError(instance: T, propertyName: String, message: String) {
        val className = instance.javaClass.canonicalName

        results
            .getOrPut(className) { mutableMapOf() }
            .getOrPut(propertyName) { mutableListOf() }
            .add(message)
    }

    fun compileResults(): List<ValidationResultInfo> {
        return results.flatMap { (className, propertyMessages) ->
            propertyMessages.flatMap { (propertyName, messages) ->
                messages.map { message ->
                    ValidationResultInfo(
                        className = className,
                        propertyName = propertyName,
                        message = message
                    )
                }
            }
        }.sortedBy { it.className + it.propertyName + it.message }
    }

    fun compileResultsToSimpleStrings(): List<String> {
        return compileResults().map { "${it.className.substringAfterLast(".")}.${it.propertyName}: ${it.message}" }
    }

    fun clear() = results.clear()
}
