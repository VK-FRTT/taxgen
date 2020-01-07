package fi.vm.yti.taxgen.dpmmodel.unitestbase

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationResultCollector
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties
import org.assertj.core.api.Assertions.assertThat

internal fun <T : Any> DpmModel_UnitTestBase<T>.propertyLengthValidationTemplate(
    propertyName: String,
    validationType: String,
    expectedLimit: Int,
    customValueBuilder: ((KProperty1<*, *>, Int) -> (Map<String, Any>))? = null
) {
    var validOverrideAttributes: Map<String, Any>
    var invalidOverrideAttributes: Map<String, Any>
    var invalidReason: String
    var invalidValueName: String

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

            invalidReason = "Too short (minimum $expectedLimit characters)"
            invalidValueName = propertyName.capitalize()
        }

        "minColLength" -> {
            validOverrideAttributes = customOverrideAttributesForProp(propertyName, expectedLimit, customValueBuilder)

            invalidOverrideAttributes =
                customOverrideAttributesForProp(propertyName, expectedLimit - 1, customValueBuilder)

            invalidReason = "Too short (minimum $expectedLimit elements)"
            invalidValueName = propertyName.capitalize()
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

            invalidReason = "Too long (maximum $expectedLimit characters)"
            invalidValueName = propertyName.capitalize()
        }

        "maxColLength" -> {
            validOverrideAttributes =
                customOverrideAttributesForProp(propertyName, expectedLimit, customValueBuilder)

            invalidOverrideAttributes =
                customOverrideAttributesForProp(propertyName, expectedLimit + 1, customValueBuilder)

            invalidReason = "Too long (maximum $expectedLimit elements)"
            invalidValueName = propertyName.capitalize()
        }

        else -> throwIllegalDpmModelState("PropertyLengthValidationTemplate does not support given validation type '$validationType'")
    }

    // Valid value
    val validAttributes = Factory.Builder.attributesFor(kClass, validOverrideAttributes)
    val valid = Factory.Builder.instantiate(kClass, validAttributes) as Validatable

    val validCollector = ValidationResultCollector()
    valid.validate(validCollector)
    assertThat(validCollector.uniqueResults()).isEmpty()

    // Invalid value
    val invalidAttributes = Factory.Builder.attributesFor(kClass, invalidOverrideAttributes)
    val invalid = Factory.Builder.instantiate(kClass, invalidAttributes) as Validatable

    val invalidCollector = ValidationResultCollector()
    invalid.validate(invalidCollector)

    val filteredResults = invalidCollector.uniqueResults().filter {
        it.valueName() == invalidValueName && it.reason() == invalidReason
    }

    assertThat(filteredResults).hasSize(1)
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
