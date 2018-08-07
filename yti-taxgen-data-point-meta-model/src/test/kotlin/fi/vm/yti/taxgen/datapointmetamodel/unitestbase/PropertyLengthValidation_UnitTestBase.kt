package fi.vm.yti.taxgen.datapointmetamodel.unitestbase

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrorCollector
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import org.assertj.core.api.Assertions.assertThat
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties

internal fun <T : Validatable> DpmModel_UnitTestBase<T>.propertyLengthValidationTemplate(
    propertyName: String,
    validationType: String,
    expectedLimit: Int,
    customValueBuilder: ((KProperty1<*, *>, Int) -> (Any?))? = null
) {
    val validValue: Any
    val invalidValue: Any
    val messageComposer: (String) -> String

    when (validationType) {
        "minLength" -> {
            validValue = stringValueForProperty(propertyName, expectedLimit)
            invalidValue = stringValueForProperty(propertyName, expectedLimit - 1)
            messageComposer =
                { className -> "$className.$propertyName: is too short (minimum $expectedLimit characters)" }
        }

        "minColLength" -> {
            validValue = customValueForProperty(propertyName, expectedLimit, customValueBuilder)
            invalidValue = customValueForProperty(propertyName, expectedLimit - 1, customValueBuilder)
            messageComposer =
                { className -> "$className.$propertyName: is too short (minimum $expectedLimit elements)" }
        }

        "maxLength" -> {
            validValue = stringValueForProperty<T>(propertyName, expectedLimit)
            invalidValue = stringValueForProperty<T>(propertyName, expectedLimit + 1)
            messageComposer =
                { className -> "$className.$propertyName: is too long (maximum $expectedLimit characters)" }
        }

        "maxColLength" -> {
            validValue = customValueForProperty<T>(propertyName, expectedLimit, customValueBuilder)
            invalidValue = customValueForProperty<T>(propertyName, expectedLimit + 1, customValueBuilder)
            messageComposer = { className -> "$className.$propertyName: is too long (maximum $expectedLimit elements)" }
        }

        else -> thisShouldNeverHappen("PropertyLengthValidationTemplate does not support given validation type '$validationType'")
    }

    //Valid value
    val validAttributes = Factory.Builder.attributesFor(kClass, mapOf(propertyName to validValue))
    val valid = Factory.Builder.instantiate(kClass, validAttributes) as Validatable

    val validCollector = ValidationErrorCollector()
    valid.validate(validCollector)
    assertThat(validCollector.errorsInSimpleFormat()).isEmpty()

    //Invalid value
    val invalidAttributes = Factory.Builder.attributesFor(kClass, mapOf(propertyName to invalidValue))
    val invalid = Factory.Builder.instantiate(kClass, invalidAttributes) as Validatable

    val invalidCollector = ValidationErrorCollector()
    invalid.validate(invalidCollector)

    val message = messageComposer(invalid.javaClass.simpleName)
    assertThat(invalidCollector.errorsInSimpleFormat()).containsOnlyOnce(message)
}

private fun <T : Validatable> DpmModel_UnitTestBase<T>.stringValueForProperty(
    propertyName: String,
    length: Int
): Any {
    val property = kClass.memberProperties.find { it.name == propertyName }
        ?: throw IllegalArgumentException("No property found for name: $propertyName from class: ${kClass.simpleName}")

    if (property.returnType.isSubtypeOf(String::class.createType())) {
        return "*".repeat(length)
    }

    if (property.returnType.isSupertypeOf(emptyList<String>()::class.createType())) {
        return List(length) { index -> index.toString() }
    }

    thisShouldNeverHappen("PropertyLengthValidationTemplate can not build value for '$propertyName' property with length $length")
}

private fun <T : Validatable> DpmModel_UnitTestBase<T>.customValueForProperty(
    propertyName: String,
    length: Int,
    customValueBuilder: ((KProperty1<T, *>, Int) -> (Any?))?
): Any {
    val property = kClass.memberProperties.find { it.name == propertyName }
        ?: throw IllegalArgumentException("No property found for name: $propertyName from class: ${kClass.simpleName}")

    if (customValueBuilder == null) {
        thisShouldNeverHappen("CustomValueBuilder not provided for '$propertyName' property")
    }

    val value = customValueBuilder(property, length)
    if (value != null) {
        return value
    }

    thisShouldNeverHappen("CustomValueBuilder did not build value for '$propertyName' property with length $length")
}
