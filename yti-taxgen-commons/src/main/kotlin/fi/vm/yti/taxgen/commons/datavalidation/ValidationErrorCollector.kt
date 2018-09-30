package fi.vm.yti.taxgen.commons.datavalidation

class ValidationErrorCollector : ValidationErrors {

    private val errors: MutableMap<String, MutableMap<String, MutableList<String>>> = mutableMapOf()

    override fun <T : Any> add(instance: T, propertyName: String, message: String) {
        val className = instance.javaClass.canonicalName

        errors
            .getOrPut(className) { mutableMapOf() }
            .getOrPut(propertyName) { mutableListOf() }
            .add(message)
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
