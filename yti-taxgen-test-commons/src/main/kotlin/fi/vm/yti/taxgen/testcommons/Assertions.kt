package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.ext.kotlin.chainToString
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import org.assertj.core.api.Assertions

fun shouldBeNullException(exception: Throwable?) {
    if (exception == null) return

    assertFail(
        message = "Unexpected exception with chain: ${exception.chainToString()}",
        cause = exception
    )
}

fun assertFail(message: String, cause: Throwable? = null): Nothing {
    Assertions.fail(message, cause)

    thisShouldNeverHappen("Unreachable, but needed to keep Kotlin compiler happy")
}
