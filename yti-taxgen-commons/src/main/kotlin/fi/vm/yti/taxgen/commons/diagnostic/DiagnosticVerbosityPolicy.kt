package fi.vm.yti.taxgen.commons.diagnostic

import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticEventFilteringPolicy
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.DEBUG
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.ERROR
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.FATAL
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.INFO
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.Severity.WARNING

class DiagnosticVerbosityPolicy(
    outputVerbosity: DiagnosticOutputVerbosity
) : DiagnosticEventFilteringPolicy {
    private val acceptedMessageSeverities = acceptedMessageSeveritiesForVerbosity(outputVerbosity)

    override fun suppressMessage(severity: Severity, message: String): Boolean {
        return !acceptedMessageSeverities.contains(severity)
    }

    private fun acceptedMessageSeveritiesForVerbosity(outputVerbosity: DiagnosticOutputVerbosity): List<Severity> {
        return when (outputVerbosity) {
            DiagnosticOutputVerbosity.NORMAL -> {
                listOf(FATAL, ERROR, WARNING, INFO)
            }
            DiagnosticOutputVerbosity.DEBUG -> {
                listOf(FATAL, ERROR, WARNING, INFO, DEBUG)
            }
        }
    }
}
