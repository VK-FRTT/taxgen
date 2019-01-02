package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface DpmDictionarySource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.DpmDictionary
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun dpmOwnerConfigData(action: (String) -> Unit)
    fun metricsSource(action: (CodeListSource?) -> Unit)
    fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit)
    fun explicitDimensionsSource(action: (CodeListSource?) -> Unit)
    fun typedDomainsSource(action: (CodeListSource?) -> Unit)
    fun typedDimensionsSource(action: (CodeListSource?) -> Unit)
}
