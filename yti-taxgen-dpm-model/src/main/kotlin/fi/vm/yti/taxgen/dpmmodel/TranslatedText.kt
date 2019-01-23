package fi.vm.yti.taxgen.dpmmodel

data class TranslatedText(
    val translations: Map<Language, String>
) {
    internal lateinit var defaultLanguage: Language

    fun defaultTranslation(): String? {
        return translations[defaultLanguage]
    }

    companion object {
        fun empty() = TranslatedText(emptyMap())
    }
}
