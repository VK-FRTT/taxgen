package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextDetails

interface DpmSource : DiagnosticContextDetails {
    fun sourceConfigData(): String
    fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit)
}
