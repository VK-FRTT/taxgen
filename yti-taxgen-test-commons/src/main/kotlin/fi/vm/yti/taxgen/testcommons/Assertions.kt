package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.ext.kotlin.chainToString
import org.assertj.core.api.Assertions

fun assertExceptionIsNull(exception: Throwable?) {
    if (exception == null) return

    failTestCase(
        "Expected no exception, but received ${exception.chainToString()}",
        exception
    )
}

fun failTestCase(description: String, cause: Throwable? = null): Nothing {
    Assertions.fail(description, cause)

    throw RuntimeException("Unreachable, but needed to keep Kotlin compiler NullSafety analyzer happy")
}
