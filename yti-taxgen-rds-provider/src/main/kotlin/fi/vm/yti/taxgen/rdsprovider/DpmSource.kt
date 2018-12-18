package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import java.io.Closeable

abstract class DpmSource : Closeable, DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.DpmSource

    abstract fun sourceConfigData(): String
    abstract fun dpmDictionarySources(): Sequence<DpmDictionarySource>
}
