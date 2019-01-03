package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextDetails
import java.io.Closeable

interface DpmSource : Closeable, DiagnosticContextDetails {
    fun sourceConfigData(): String
    fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit)
}
