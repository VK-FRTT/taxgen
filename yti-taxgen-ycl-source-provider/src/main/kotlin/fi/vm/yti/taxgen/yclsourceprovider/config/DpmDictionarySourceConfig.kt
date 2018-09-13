package fi.vm.yti.taxgen.yclsourceprovider.config

data class DpmDictionarySourceConfig(
    val owner: OwnerConfig,
    val codelists: List<YclCodelistSourceConfig>
)
