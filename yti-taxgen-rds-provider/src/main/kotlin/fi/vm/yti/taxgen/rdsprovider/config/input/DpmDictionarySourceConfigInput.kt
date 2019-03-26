package fi.vm.yti.taxgen.rdsprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.config.DpmDictionarySourceConfig

@Suppress("MemberVisibilityCanBePrivate")
data class DpmDictionarySourceConfigInput(
    val owner: OwnerConfigInput?,
    val metrics: CodeListConfigInput?,
    val explicitDomainsAndHierarchies: CodeListConfigInput?,
    val explicitDimensions: CodeListConfigInput?,
    val typedDomains: CodeListConfigInput?,
    val typedDimensions: CodeListConfigInput?
) {
    fun toConfig(diagnostic: Diagnostic): DpmDictionarySourceConfig {
        validateValueNotNull(this::owner, diagnostic)
        validateValueNotNull(this::metrics, diagnostic)
        validateValueNotNull(this::explicitDomainsAndHierarchies, diagnostic)
        validateValueNotNull(this::explicitDimensions, diagnostic)
        validateValueNotNull(this::typedDomains, diagnostic)
        validateValueNotNull(this::typedDimensions, diagnostic)

        return DpmDictionarySourceConfig(
            owner = owner!!.toConfig(diagnostic),
            metrics = metrics!!.toConfig(diagnostic),
            explicitDomainsAndHierarchies = explicitDomainsAndHierarchies!!.toConfig(diagnostic),
            explicitDimensions = explicitDimensions!!.toConfig(diagnostic),
            typedDomains = typedDomains!!.toConfig(diagnostic),
            typedDimensions = typedDimensions!!.toConfig(diagnostic)
        )
    }
}
