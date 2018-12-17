package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType

interface DpmDictionarySource : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.DpmDictionary
    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    fun dpmOwnerConfigData(): String
    fun metricsSource(): CodeListSource?
    fun explicitDomainsAndHierarchiesSource(): CodeListSource?
    fun explicitDimensionsSource(): CodeListSource?
    fun typedDomainsSource(): CodeListSource?
    fun typedDimensionsSource(): CodeListSource?
}
