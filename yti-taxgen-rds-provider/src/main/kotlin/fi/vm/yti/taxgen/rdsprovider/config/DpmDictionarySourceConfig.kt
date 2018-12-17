package fi.vm.yti.taxgen.rdsprovider.config

data class DpmDictionarySourceConfig(
    val owner: OwnerConfig,
    val metrics: CodeListConfig,
    val explicitDomainsAndHierarchies: CodeListConfig,
    val explicitDimensions: CodeListConfig,
    val typedDomains: CodeListConfig,
    val typedDimensions: CodeListConfig
)
