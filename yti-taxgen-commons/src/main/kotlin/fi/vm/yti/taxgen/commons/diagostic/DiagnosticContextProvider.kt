package fi.vm.yti.taxgen.commons.diagostic

interface DiagnosticContextProvider {
    fun contextType(): String
    fun contextName(): String
    fun contextRef(): String
}
