package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import kotlin.reflect.KProperty0

fun validatePropTranslatedText(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<TranslatedText>,
    minTranslationLength: Int? = null,
    minLangCount: Int? = null,
    acceptedLanguages: Set<Language>? = null
) {
    val translatedText = property.get()

    if (minTranslationLength != null) {
        val shortTranslationLanguages = translatedText.translations
            .filter { it.value.trim().length < minTranslationLength }
            .keys.map { it.iso6391Code }
            .sorted()

        if (shortTranslationLanguages.any()) {
            validationResultBuilder.addError(
                valueName = property,
                reason = "Too short translation content for languages",
                value = shortTranslationLanguages.joinToString()
            )
        }
    }

    if (minLangCount != null) {
        if (translatedText.translations.size < minLangCount) {
            validationResultBuilder.addError(
                valueName = property,
                reason = "Too few translations (minimum $minLangCount)"
            )
        }
    }

    if (acceptedLanguages != null) {
        val translationLanguages = translatedText.translations.keys
        val surplusLanguages = translationLanguages - acceptedLanguages

        if (surplusLanguages.any()) {
            validationResultBuilder.addError(
                valueName = property,
                reason = "Surplus translation languages",
                value = surplusLanguages.map { it.iso6391Code }.sorted().joinToString()
            )
        }
    }
}
