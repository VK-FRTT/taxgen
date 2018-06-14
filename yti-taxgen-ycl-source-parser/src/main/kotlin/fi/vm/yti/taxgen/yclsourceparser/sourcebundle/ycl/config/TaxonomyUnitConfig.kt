package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl.config

data class TaxonomyUnitConfig(
    val namespace: String,
    val namespacePrefix: String,
    val officialLocation: String,
    val copyrightText: String,
    val supportedLanguages: List<String>,
    val codeLists: List<CodeListConfig>
)
