package fi.vm.yti.taxgen.yclsourceprovider.ycl.config

data class TaxonomyUnitConfig(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val supportedLanguages: List<String>,
    val codeLists: List<CodeListConfig>
)
