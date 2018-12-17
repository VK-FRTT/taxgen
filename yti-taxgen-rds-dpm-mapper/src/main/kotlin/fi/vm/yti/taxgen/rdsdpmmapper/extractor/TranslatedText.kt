package fi.vm.yti.taxgen.rdsdpmmapper.extractor

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.TranslatedText

internal fun TranslatedText.Companion.fromYclLangText(
    langText: Map<String, String>?,
    languageSet: Set<Language>
): TranslatedText {

    fun resolveTranslations(): Map<Language, String> {
        if (langText == null) {
            return emptyMap()
        }

        return langText
            .map { (langCode, text) ->
                val language = languageSet.find { it.iso6391Code == langCode }

                if (language == null) {
                    null
                } else {
                    Pair(language, text)
                }
            }
            .filterNotNull()
            .toMap()
    }

    return TranslatedText(resolveTranslations())
}
