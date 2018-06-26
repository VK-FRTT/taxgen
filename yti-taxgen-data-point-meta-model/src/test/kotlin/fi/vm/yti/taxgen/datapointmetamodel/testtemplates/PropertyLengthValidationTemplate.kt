package fi.vm.yti.taxgen.datapointmetamodel.testtemplates

import fi.vm.yti.taxgen.commons.ext.kotlin.chainToString
import fi.vm.yti.taxgen.commons.ext.kotlin.rootExceptionOfClass
import fi.vm.yti.taxgen.datapointmetamodel.testdataframework.Factory
import fi.vm.yti.taxgen.datapointmetamodel.validationfw.DataValidationException
import fi.vm.yti.taxgen.testcommons.assertExceptionIsNull
import fi.vm.yti.taxgen.testcommons.failTestCase
import org.assertj.core.api.Assertions
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> propertyLengthValidationTemplate(
    testedProperty: String,
    validationType: String,
    expectedLimit: Int
) {

    var validPropertyValue: Any
    var invalidPropertyValue: Any

    when (validationType) {
        "minLength" -> {
            validPropertyValue = generateTestValueForMemberProperty<T>(testedProperty, expectedLimit)
            invalidPropertyValue = generateTestValueForMemberProperty<T>(testedProperty, expectedLimit - 1)
        }

        "maxLength" -> {
            validPropertyValue = generateTestValueForMemberProperty<T>(testedProperty, expectedLimit)
            invalidPropertyValue = generateTestValueForMemberProperty<T>(testedProperty, expectedLimit + 1)
        }

        else -> throw UnsupportedOperationException("Unsupported validation type: $validationType")
    }

    val validAttributes = Factory.attributesFor<T>(overrides = mapOf(testedProperty to validPropertyValue))
    val validAttributesException = Assertions.catchThrowable { Factory.instantiate<T>(validAttributes) }
    assertExceptionIsNull(validAttributesException)

    val invalidAttributes = Factory.attributesFor<T>(overrides = mapOf(testedProperty to invalidPropertyValue))
    val invalidAttributesException = Assertions.catchThrowable { Factory.instantiate<T>(invalidAttributes) }
    assertExceptionIsDataValidationFailedException(
        invalidAttributesException,
        testedProperty,
        validationType
    )
}

inline fun <reified T : Any> generateTestValueForMemberProperty(
    propertyName: String,
    length: Int
): Any {
    val property = T::class.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("No property found for name: $propertyName from class: ${T::class.simpleName}")

    if (property.returnType.isSubtypeOf(String::class.createType())) {
        return "*".repeat(length)
    }

    if (property.returnType.isSupertypeOf(emptyList<String>()::class.createType())) {
        return List(length) { index -> index.toString() }
    }

    throw UnsupportedOperationException("Unsupported value type in property: $propertyName")
}

fun assertExceptionIsDataValidationFailedException(
    exception: Throwable?,
    expectedFailingProperty: String,
    expectedValidatorType: String
) {
    if (exception == null) {
        failTestCase(
            "$expectedFailingProperty: Expected Data Validation Exception but none received",
            exception
        )
    }

    val rootException =
        exception.rootExceptionOfClass<DataValidationException>()
                ?: failTestCase(
                    "$expectedFailingProperty: Expected DataValidationException root exception " +
                            "not present in exception chain: ${exception.chainToString()}",
                    exception
                )

    if (!Regex("\\[$expectedFailingProperty: .*").matches(rootException.message)) {
        failTestCase(
            "$expectedFailingProperty: Expected failing property name ($expectedFailingProperty) " +
                    "not present in exception message: ${rootException.message}",
            exception
        )
    }

    if (!Regex(".*: $expectedValidatorType .*").matches(rootException.message)) {
        failTestCase(
            "$expectedFailingProperty: Expected validator type ($expectedValidatorType) " +
                    "not present in exception message: ${rootException.message}",
            exception
        )
    }
}
