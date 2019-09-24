package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextDetails

interface DpmSource : DiagnosticContextDetails {
    fun config(): DpmSourceConfigHolder
    fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit)
}
