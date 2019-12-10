package fi.vm.yti.taxgen.rdsource

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails

interface DpmSource : DiagnosticContextDetails {
    fun config(): DpmSourceConfigHolder
    fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit)
}
