package fi.vm.yti.taxgen.dpmmodel

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

    fun anyNonBlankTranslation(): String? {
        return translations.entries.firstOrNull { it.value.isNotBlank() }?.value
    }
}
