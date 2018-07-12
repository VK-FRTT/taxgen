package fi.vm.yti.taxgen.datapointmetamodel

data class TranslatedText(
    val translations: Map<Language, String>
) {
    internal lateinit var defaultLanguage: Language

    fun defaultText(): String? {
        return translations[defaultLanguage]
    }
}
