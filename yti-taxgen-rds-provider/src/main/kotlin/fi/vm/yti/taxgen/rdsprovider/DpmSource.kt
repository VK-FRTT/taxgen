package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails

interface DpmSource : DiagnosticContextDetails {
    fun config(): DpmSourceConfigHolder
    fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit)
}
