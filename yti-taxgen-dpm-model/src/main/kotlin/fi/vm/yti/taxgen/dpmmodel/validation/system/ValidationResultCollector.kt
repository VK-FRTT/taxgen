package fi.vm.yti.taxgen.dpmmodel.validation.system

import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.validation.ValidatableNestedObject
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

class ValidationResultCollector : ValidationResultBuilder {

    private val results: MutableList<ValidationResultDescriptor> = mutableListOf()
    private val subjectStack = LinkedList<ValidationSubjectDescriptor>()
    private val nameStack = LinkedList<String>()

    override fun addError(
        valueName: Any,
        reason: String
    ) {
        val result = ValidationResultDescriptor.withEssentials(
            subjects = subjectsStackToList(),
            valueName = combineValueName(valueName),
            reason = reason
        )

        results.add(result)
    }

    override fun addError(
        valueName: Any,
        reason: String,
        value: String
    ) {
        val result = ValidationResultDescriptor.withEssentialsAndValue(
            subjects = subjectsStackToList(),
            valueName = combineValueName(valueName),
            reason = reason,
            value = value
        )

        results.add(result)
    }

    override fun addError(
        subject: ValidationSubjectDescriptor,
        valueName: Any,
        reason: String,
        value: String
    ) {
        withSubject(subject) {
            val result = ValidationResultDescriptor.withEssentialsAndValue(
                subjects = subjectsStackToList(),
                valueName = combineValueName(valueName),
                reason = reason,
                value = value
            )

            results.add(result)
        }
    }

    override fun withSubject(subject: ValidationSubjectDescriptor, block: () -> Unit) {
        subjectStack.push(subject)

        block()

        subjectStack.pop()
    }

    override fun withNestedObject(objectClass: KClass<*>, block: () -> Unit) {
        val name = objectClass.simpleName?.capitalize() ?: throwIllegalDpmModelState("Anonymous nested object")

        nameStack.push(name)

        block()

        nameStack.pop()
    }

    override fun <T : ValidatableNestedObject> validateNestedProperty(property: KProperty0<T>) {
        val name = property.name.capitalize()

        nameStack.push(name)

        property.get().validate(this)

        nameStack.pop()
    }

    fun uniqueResults(): List<ValidationResultDescriptor> = results.distinct()

    private fun combineValueName(valueName: Any): String {
        val valueNameString = stringifyValueName(valueName)
        return nameStack.fold(valueNameString) { combinedNames, nameElement -> "$nameElement.$combinedNames" }
    }

    private fun subjectsStackToList(): List<ValidationSubjectDescriptor> {
        return subjectStack.asReversed().map { it }
    }

    companion object {
        fun stringifyValueName(valueName: Any?): String {
            return when (valueName) {
                is String -> valueName
                is KProperty0<*> -> valueName.name.capitalize()
                is KProperty1<*, *> -> valueName.name.capitalize()
                is KClass<*> -> valueName.simpleName ?: throwIllegalDpmModelState("Unsupported anonymous class")
                is List<*> -> valueName.joinToString(separator = ".") { stringifyValueName(it) }
                else -> throwIllegalDpmModelState("Unsupported valueName: $valueName")
            }
        }
    }
}
