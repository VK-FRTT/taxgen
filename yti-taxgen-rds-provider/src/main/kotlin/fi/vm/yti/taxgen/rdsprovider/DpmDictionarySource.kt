package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextDetails

interface DpmDictionarySource : DiagnosticContextDetails {
    fun dpmOwner(action: (OwnerHolder) -> Unit)
    fun metricsSource(action: (CodeListSource?) -> Unit)
    fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit)
    fun explicitDimensionsSource(action: (CodeListSource?) -> Unit)
    fun typedDomainsSource(action: (CodeListSource?) -> Unit)
    fun typedDimensionsSource(action: (CodeListSource?) -> Unit)
}
