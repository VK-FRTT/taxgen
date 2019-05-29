package fi.vm.yti.taxgen.dpmmodel

data class TranslatedText(
    val translations: Map<Language, String>
) {
    internal lateinit var defaultLanguage: Language

    fun defaultTranslationOrNull(): String? {
        return translations[defaultLanguage]
    }

    fun highestPriorityTranslationOrNull(): String? {
        return defaultTranslationOrNull() ?: selectHighestPriorityTranslation()
    }

    private fun selectHighestPriorityTranslation(): String? {
        val prioritizedLanguage = Language.findHighestPriorityLanguage(translations.keys)
        return translations[prioritizedLanguage]
    }

    companion object {
        fun empty() = TranslatedText(emptyMap())
    }
}
