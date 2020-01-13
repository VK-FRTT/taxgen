package fi.vm.yti.taxgen.dpmmodel.diagnostic.system

interface DiagnosticEventFilteringPolicy {

    fun suppressMessage(severity: Severity, message: String): Boolean
}
