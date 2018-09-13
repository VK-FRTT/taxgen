package fi.vm.yti.taxgen.yclsourceprovider.config

data class OwnerConfig(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val languages: List<String>,
    val defaultLanguage: String
)
