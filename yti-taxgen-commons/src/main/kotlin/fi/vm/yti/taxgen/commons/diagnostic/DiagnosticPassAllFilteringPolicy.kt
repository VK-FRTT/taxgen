package fi.vm.yti.taxgen.commons.diagnostic

import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticEventFilteringPolicy
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity

class DiagnosticPassAllFilteringPolicy : DiagnosticEventFilteringPolicy {

    override fun suppressMessage(severity: Severity, message: String): Boolean = false
}
