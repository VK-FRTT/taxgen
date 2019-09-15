package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextDetails
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfigHolder

interface DpmSource : DiagnosticContextDetails {
    fun config(): DpmSourceConfigHolder
    fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit)
}
