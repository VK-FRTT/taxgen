package fi.vm.yti.taxgen.datapointmetamodel

data class TranslatedText(
    val translations: Map<Language, String>
) {
    companion object {
        fun none() = TranslatedText(translations = emptyMap())
    }

    internal lateinit var defaultLanguage: Language

    fun defaultTranslation(): String? {
        return translations[defaultLanguage]
    }

    fun anyTranslation(): Pair<Language, String>? {

        translations.entries.forEach {
            return it.toPair()
        }

        return null
    }
}
