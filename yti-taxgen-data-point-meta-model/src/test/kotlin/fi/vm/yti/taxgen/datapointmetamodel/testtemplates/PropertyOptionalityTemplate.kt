package fi.vm.yti.taxgen.datapointmetamodel.testtemplates

import fi.vm.yti.taxgen.commons.platformextension.getPropertyValue
import fi.vm.yti.taxgen.commons.platformextension.chainToString
import fi.vm.yti.taxgen.commons.platformextension.rootExceptionOfClass
import fi.vm.yti.taxgen.datapointmetamodel.testdataframework.Factory
import fi.vm.yti.taxgen.commons.test.helpers.assertExceptionIsNull
import fi.vm.yti.taxgen.commons.test.helpers.failTestCase
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat


inline fun <reified T : Any> propertyOptionalityTemplate(
    testedProperty: String,
    expectedOptionality: String
) {
    //1. Test property content existence
    val defaultAttributes = Factory.attributesFor<T>()
    val instance = Factory.instantiate<T>(defaultAttributes)
    val propertyValue = instance.getPropertyValue(testedProperty)

    assertThat(propertyValue)
        .isNotNull()
        .isEqualTo(defaultAttributes[testedProperty])


    //2. Test property optionality
    val attributes = Factory.attributesFor<T>(
        overrides = mapOf(testedProperty to null)
    )

    val exception = Assertions.catchThrowable { Factory.instantiate<T>(attributes) }

    when (expectedOptionality) {
        "required" -> assertExceptionIsRequiredPropertyValueMissingException(
            exception,
            testedProperty
        )
        "optional" -> assertExceptionIsNull(exception)
        else -> throw UnsupportedOperationException("Unsupported optionality: $expectedOptionality")
    }
}


fun assertExceptionIsRequiredPropertyValueMissingException(
    exception: Throwable?,
    expectedFailingProperty: String
) {
    if (exception == null) {
        failTestCase(
            "$expectedFailingProperty: Expected exception for but none received",
            exception
        )
    }

    val rootException =
        exception.rootExceptionOfClass<IllegalArgumentException>()
                ?: failTestCase(
                    "$expectedFailingProperty: Expected IllegalArgumentException root exception " +
                            "not found from the exception chain: ${exception.chainToString()}",
                    exception
                )

    val message = rootException.message

    if (message != null) {
        if (!Regex(".*parameter $expectedFailingProperty").matches(message)) {
            failTestCase(
                "$expectedFailingProperty: Expected failing property name ($expectedFailingProperty) " +
                        "not present in exception message: $message",
                exception
            )
        }
    }
}
