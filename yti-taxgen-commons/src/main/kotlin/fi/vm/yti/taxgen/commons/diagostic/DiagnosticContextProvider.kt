package fi.vm.yti.taxgen.commons.diagostic

interface DiagnosticContextProvider {
    fun contextType(): DiagnosticContextType
    fun contextLabel(): String
    fun contextIdentifier(): String
}
