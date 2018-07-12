package fi.vm.yti.taxgen.ycltodpmmapper.mappers

import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText

object TranslatedTextMapper {

    fun fromYclLangText(langText: Map<String, String>?): TranslatedText {
        langText ?: return TranslatedText(emptyMap())

        val translations = langText
            .map { (yclLangCode, text) ->
                val language = Language.findByIso6391Code(yclLangCode)

                if (language == null) {
                    null
                } else {
                    Pair(language, text)
                }
            }
            .filterNotNull()
            .toMap()

        return TranslatedText(translations)
    }
}
