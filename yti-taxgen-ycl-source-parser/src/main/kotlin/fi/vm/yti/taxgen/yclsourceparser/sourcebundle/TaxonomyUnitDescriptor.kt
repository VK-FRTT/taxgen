package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

data class TaxonomyUnitDescriptor(
    val namespace: String,
    val namespacePrefix: String,
    val officialLocation: String,
    val copyrightText: String,
    val supportedLanguages: List<String>
)
