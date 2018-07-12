package fi.vm.yti.taxgen.datapointmetamodel

data class OwnerConfig(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val languages: List<String>,
    val defaultLanguage: String
) {

    fun toOwner(): Owner {
        val mappedLanguages = languages.map { Language.findByIso6391Code(it)!! }  //TODO
        val mappedDefaultLanguage = Language.findByIso6391Code(defaultLanguage)!! //TODO

        return Owner(
            name = name,
            namespace = namespace,
            prefix = prefix,
            location = location,
            copyright = copyright,
            languages = mappedLanguages,
            defaultLanguage = mappedDefaultLanguage
        )
    }
}
