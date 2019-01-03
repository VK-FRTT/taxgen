package fi.vm.yti.taxgen.commons.diagostic

data class ContextInfo(
    val type: DiagnosticContextType,
    val recurrenceIndex: Int,
    val label: String,
    val identifier: String
)
