package fi.vm.yti.taxgen.dpmmodel.unitestbase

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.ext.kotlin.chainToString
import fi.vm.yti.taxgen.dpmmodel.ext.kotlin.getPropertyValue
import fi.vm.yti.taxgen.dpmmodel.ext.kotlin.rootExceptionOfClass
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable

internal fun <T : Any> DpmModel_UnitTestBase<T>.propertyOptionalityTemplate(
    propertyName: String,
    expectedOptionality: String
) {
    //1. Verify that given property really exists within tested class
    val defaultAttributes = Factory.Builder.attributesFor(kClass, emptyMap())
    val instance = Factory.Builder.instantiate(kClass, defaultAttributes)
    val propertyValue = instance.getPropertyValue(propertyName)

    assertThat(propertyValue)
        .isNotNull()
        .isEqualTo(defaultAttributes[propertyName])

    //2. Test is the property optional/required
    val attributes = Factory.Builder.attributesFor(
        kClass = kClass,
        overrideAttributes = mapOf(propertyName to null)
    )

    val exception = catchThrowable {
        Factory.Builder.instantiate(kClass, attributes)
    }

    when (expectedOptionality) {
        "required" -> shouldBeRequiredPropertyValueMissingException(exception, propertyName)
        "optional" -> shouldBeNullException(exception)

        else -> throwIllegalDpmModelState("PropertyOptionalityTemplate does not support given optionality: $expectedOptionality")
    }
}

private fun shouldBeRequiredPropertyValueMissingException(
    exception: Throwable?,
    failingProperty: String
) {
    if (exception == null) {
        assertFail(
            message = "$failingProperty: No exception thrown"
        )
    }

    val rootException = exception.rootExceptionOfClass<IllegalArgumentException>()
    if (rootException == null) {
        assertFail(
            message = "$failingProperty: Exception chain not having IllegalArgumentException at root: ${exception.chainToString()}",
            cause = exception
        )
    }

    val message = rootException.message
    if (message != null) {
        if (!Regex(".*parameter $failingProperty").matches(message)) {
            assertFail(
                message = "$failingProperty: Property name not present in exception message: $message",
                cause = exception
            )
        }
    }
}

private fun shouldBeNullException(exception: Throwable?) {
    if (exception == null) return

    assertFail(
        message = "Unexpected exception with chain: ${exception.chainToString()}",
        cause = exception
    )
}

private fun assertFail(message: String, cause: Throwable? = null): Nothing {
    Assertions.fail(message, cause)
    throwIllegalDpmModelState("Unreachable")
}
