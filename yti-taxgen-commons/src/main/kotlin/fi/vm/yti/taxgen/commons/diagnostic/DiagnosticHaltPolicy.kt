package fi.vm.yti.taxgen.commons.diagnostic

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.throwHalt
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticProcesStoppingPolicy
import java.io.PrintWriter
import java.io.StringWriter

class DiagnosticHaltPolicy : DiagnosticProcesStoppingPolicy {

    override fun exceptionCaughtStopProcessing(ex: Exception, diagnostic: Diagnostic): Nothing {

        when (ex) {
            is HaltException -> {
                throw ex
            }

            is FailException -> {
                throw ex
            }

            else -> {
                val sw = StringWriter()
                ex.printStackTrace(PrintWriter(sw))
                diagnostic.fatal("Internal error. $sw")
            }
        }
    }

    override fun stopProcessing(): Nothing {
        throwHalt()
    }
}
