package fi.vm.yti.taxgen.rdsource

import fi.vm.yti.taxgen.dpmmodel.Owner

data class DpmDictionarySourceConfig(
    val owner: Owner,
    val metrics: CodeListConfig,
    val explicitDomainsAndHierarchies: CodeListConfig,
    val explicitDimensions: CodeListConfig,
    val typedDomains: CodeListConfig,
    val typedDimensions: CodeListConfig
)
