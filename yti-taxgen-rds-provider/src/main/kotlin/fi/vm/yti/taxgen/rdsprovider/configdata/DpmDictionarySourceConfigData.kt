package fi.vm.yti.taxgen.rdsprovider.configdata

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySourceConfig

@Suppress("MemberVisibilityCanBePrivate")
data class DpmDictionarySourceConfigData(
    val owner: OwnerConfigData?,
    val metrics: CodeListConfigData?,
    val explicitDomainsAndHierarchies: CodeListConfigData?,
    val explicitDimensions: CodeListConfigData?,
    val typedDomains: CodeListConfigData?,
    val typedDimensions: CodeListConfigData?
) {
    fun toConfig(diagnostic: Diagnostic): DpmDictionarySourceConfig {
        validateValueNotNull(this::owner, diagnostic)
        validateValueNotNull(this::metrics, diagnostic)
        validateValueNotNull(this::explicitDomainsAndHierarchies, diagnostic)
        validateValueNotNull(this::explicitDimensions, diagnostic)
        validateValueNotNull(this::typedDomains, diagnostic)
        validateValueNotNull(this::typedDimensions, diagnostic)

        return DpmDictionarySourceConfig(
            owner = owner!!.toOwner(diagnostic),
            metrics = metrics!!.toConfig(diagnostic),
            explicitDomainsAndHierarchies = explicitDomainsAndHierarchies!!.toConfig(diagnostic),
            explicitDimensions = explicitDimensions!!.toConfig(diagnostic),
            typedDomains = typedDomains!!.toConfig(diagnostic),
            typedDimensions = typedDimensions!!.toConfig(diagnostic)
        )
    }
}
