package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config

data class YclTaxonomyUnitConfig(
    val namespace: String,
    val namespacePrefix: String,
    val officialLocation: String,
    val copyrightText: String,
    val supportedLanguages: List<String>,
    val codeLists: List<YclCodeListConfig>
)
