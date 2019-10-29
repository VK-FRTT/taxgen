package fi.vm.yti.taxgen.dpmmodel.diagnostic.system

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic

interface DiagnosticProcesStoppingPolicy {
    fun exceptionCaughtStopProcessing(ex: Exception, diagnostic: Diagnostic): Nothing
    fun stopProcessing(): Nothing
}
