package fi.vm.yti.taxgen.yclsourceprovider

data class TaxonomyUnitInfo(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val supportedLanguages: List<String>
)
