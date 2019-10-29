package fi.vm.yti.taxgen.dpmmodel.unitestbase

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.system.ValidationCollector
import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import org.assertj.core.api.Assertions.assertThat
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties

internal fun <T : Any> DpmModel_UnitTestBase<T>.propertyLengthValidationTemplate(
    propertyName: String,
    validationType: String,
    expectedLimit: Int,
    customValueBuilder: ((KProperty1<*, *>, Int) -> (Map<String, Any>))? = null
) {
    var validOverrideAttributes: Map<String, Any>
    var invalidOverrideAttributes: Map<String, Any>
    val messageComposer: (String) -> String

    when (validationType) {
        "minLength" -> {
            validOverrideAttributes =
                tryCustomOverrideAttributesForProp(propertyName, expectedLimit, customValueBuilder)
            if (!validOverrideAttributes.containsKey(propertyName)) {
                validOverrideAttributes = stringOverrideAttributeForProp(propertyName, expectedLimit)
            }

            invalidOverrideAttributes =
                tryCustomOverrideAttributesForProp(propertyName, expectedLimit - 1, customValueBuilder)
            if (!invalidOverrideAttributes.containsKey(propertyName)) {
                invalidOverrideAttributes = stringOverrideAttributeForProp(propertyName, expectedLimit - 1)
            }

            messageComposer =
                { className -> "$className.$propertyName: is too short (minimum $expectedLimit characters)" }
        }

        "minColLength" -> {
            validOverrideAttributes = customOverrideAttributesForProp(propertyName, expectedLimit, customValueBuilder)
            invalidOverrideAttributes =
                customOverrideAttributesForProp(propertyName, expectedLimit - 1, customValueBuilder)
            messageComposer =
                { className -> "$className.$propertyName: is too short (minimum $expectedLimit elements)" }
        }

        "maxLength" -> {
            validOverrideAttributes =
                tryCustomOverrideAttributesForProp(propertyName, expectedLimit, customValueBuilder)
            if (!validOverrideAttributes.containsKey(propertyName)) {
                validOverrideAttributes = stringOverrideAttributeForProp(propertyName, expectedLimit)
            }

            invalidOverrideAttributes =
                tryCustomOverrideAttributesForProp(propertyName, expectedLimit + 1, customValueBuilder)
            if (!invalidOverrideAttributes.containsKey(propertyName)) {
                invalidOverrideAttributes = stringOverrideAttributeForProp(propertyName, expectedLimit + 1)
            }
            messageComposer =
                { className -> "$className.$propertyName: is too long (maximum $expectedLimit characters)" }
        }

        "maxColLength" -> {
            validOverrideAttributes =
                customOverrideAttributesForProp(propertyName, expectedLimit, customValueBuilder)
            invalidOverrideAttributes =
                customOverrideAttributesForProp(propertyName, expectedLimit + 1, customValueBuilder)
            messageComposer = { className -> "$className.$propertyName: is too long (maximum $expectedLimit elements)" }
        }

        else -> throwIllegalDpmModelState("PropertyLengthValidationTemplate does not support given validation type '$validationType'")
    }

    //Valid value
    val validAttributes = Factory.Builder.attributesFor(kClass, validOverrideAttributes)
    val valid = Factory.Builder.instantiate(kClass, validAttributes) as Validatable

    val validCollector = ValidationCollector()
    valid.validate(validCollector)
    assertThat(validCollector.compileResultsToSimpleStrings()).isEmpty()

    //Invalid value
    val invalidAttributes = Factory.Builder.attributesFor(kClass, invalidOverrideAttributes)
    val invalid = Factory.Builder.instantiate(kClass, invalidAttributes) as Validatable

    val invalidCollector = ValidationCollector()
    invalid.validate(invalidCollector)

    val message = messageComposer(invalid.javaClass.simpleName)
    assertThat(invalidCollector.compileResultsToSimpleStrings()).containsOnlyOnce(message)
}

private fun <T : Any> DpmModel_UnitTestBase<T>.stringOverrideAttributeForProp(
    propertyName: String,
    length: Int
): Map<String, Any> {
    val property = kClass.memberProperties.find { it.name == propertyName }
        ?: throw IllegalArgumentException("No property found for name: $propertyName from class: ${kClass.simpleName}")

    if (property.returnType.isSubtypeOf(String::class.createType())) {
        return mapOf(propertyName to "a".repeat(length))
    }

    if (property.returnType.isSupertypeOf(emptyList<String>()::class.createType())) {
        return mapOf(propertyName to List(length) { index -> index.toString() })
    }

    throwIllegalDpmModelState("Can not build value for '$propertyName' property with length $length")
}

private fun <T : Any> DpmModel_UnitTestBase<T>.customOverrideAttributesForProp(
    propertyName: String,
    length: Int,
    customValueBuilder: ((KProperty1<T, *>, Int) -> (Map<String, Any>))?
): Map<String, Any> {
    val property = kClass.memberProperties.find { it.name == propertyName }
        ?: throw IllegalArgumentException("No property found for name: $propertyName from class: ${kClass.simpleName}")

    if (customValueBuilder == null) {
        throwIllegalDpmModelState("CustomValueBuilder not provided for '$propertyName' property")
    }

    val attributes = customValueBuilder(property, length)

    if (!attributes.containsKey(propertyName)) {
        throwIllegalDpmModelState("CustomValueBuilder did not build value for '$propertyName' property with length $length")
    }

    return attributes
}

private fun <T : Any> DpmModel_UnitTestBase<T>.tryCustomOverrideAttributesForProp(
    propertyName: String,
    length: Int,
    customValueBuilder: ((KProperty1<T, *>, Int) -> (Map<String, Any>))?
): Map<String, Any> {
    val property = kClass.memberProperties.find { it.name == propertyName }
        ?: throw IllegalArgumentException("No property found for name: $propertyName from class: ${kClass.simpleName}")

    if (customValueBuilder != null) {
        return customValueBuilder(property, length)
    }

    return emptyMap()
}
