package fi.vm.yti.taxgen.rdsprovider

interface SourceProvider {
    fun withDpmSource(action: (DpmSource) -> Unit)
}
