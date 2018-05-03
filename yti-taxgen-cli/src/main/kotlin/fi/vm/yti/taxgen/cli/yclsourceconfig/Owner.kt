package fi.vm.yti.taxgen.cli.yclsourceconfig

data class Owner(
    val namespace: String,
    val namespacePrefix: String,
    val officialLocation: String,
    val copyrightText: String?,
    val supportedLanguages: List<String>
)
