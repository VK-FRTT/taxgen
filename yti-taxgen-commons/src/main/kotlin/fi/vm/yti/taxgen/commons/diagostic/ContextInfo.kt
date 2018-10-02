package fi.vm.yti.taxgen.commons.diagostic

data class ContextInfo(
    val type: DiagnosticContextType,
    val index: Int,
    val label: String,
    val identifier: String
)
