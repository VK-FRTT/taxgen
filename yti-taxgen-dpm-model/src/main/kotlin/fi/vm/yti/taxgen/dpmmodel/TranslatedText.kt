package fi.vm.yti.taxgen.dpmmodel

data class TranslatedText(
    val translations: Map<Language, String>
) {
    companion object {
        fun empty() = TranslatedText(emptyMap())
    }

    fun translationForLangOrNull(language: Language?): String? {
        language ?: return null
        return translations[language]
    }

    fun translationWithPostixForAnyLangOrNull(languages: Set<Language>): String? {
        val language = languages.firstOrNull() { it in translations }
        language ?: return null

        val text = translationForLangOrNull(language)
        text ?: return null

        return "$text (${language.iso6391Code})"
    }

    fun translationForIso6391CodeOrNull(iso6391Code: String): String? {
        val language = Language.findByIso6391Code(iso6391Code)
        return translationForLangOrNull(language)
    }
}
