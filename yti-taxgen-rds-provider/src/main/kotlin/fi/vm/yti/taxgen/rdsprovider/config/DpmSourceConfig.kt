package fi.vm.yti.taxgen.rdsprovider.config

data class DpmSourceConfig(
    val configFilePath: String,
    val rawConfigData: String,
    val dpmDictionaries: List<DpmDictionarySourceConfig>
)
