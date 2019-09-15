package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.HaltException

object ExceptionHarness {

    fun withHaltExceptionHarness(
        diagnosticCollector: DiagnosticCollector,
        exceptionIsExpected: Boolean,
        action: () -> Unit
    ) {
        try {
            action()
        } catch (exception: HaltException) {
            if (!exceptionIsExpected) {
                println(diagnosticCollector.eventsString())
            }

            throw exception
        }
    }
}
