package fi.vm.yti.taxgen.commons.diagostic

interface DiagnosticContextProvider {
    fun contextType(): DiagnosticContextType
    fun contextName(): String
    fun contextRef(): String
}
