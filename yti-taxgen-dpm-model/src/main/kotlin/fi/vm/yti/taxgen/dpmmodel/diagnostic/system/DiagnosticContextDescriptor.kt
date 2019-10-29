package fi.vm.yti.taxgen.dpmmodel.diagnostic.system

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextType

data class DiagnosticContextDescriptor(
    val contextType: DiagnosticContextType,
    val recurrenceIndex: Int,
    val contextTitle: String,
    val contextIdentifier: String
)
