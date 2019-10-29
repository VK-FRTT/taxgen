package fi.vm.yti.taxgen.dpmmodel.datavalidation

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import kotlin.reflect.KProperty1

@Suppress("FINAL_UPPER_BOUND")
fun <I : Any, P : TranslatedText> validateTranslatedText(
    validationResults: ValidationResults,
    instance: I,
    property: KProperty1<I, P>,
    minTranslationLength: Int? = null,
    minLangCount: Int? = null,
    acceptedLanguages: Set<Language>? = null
) {
    val translatedText: P = property.getter.call(instance)

    if (minTranslationLength != null) {
        val shortTranslationLanguages =
            translatedText.translations.filter { it.value.trim().length < minTranslationLength }
                .keys.map { it.iso6391Code }.sorted()
        if (shortTranslationLanguages.any()) {
            validationResults.addError(
                instance = instance,
                propertyName = property.name,
                message = "has too short translations for languages $shortTranslationLanguages"
            )
        }
    }

    if (minLangCount != null) {
        if (translatedText.translations.size < minLangCount) {
            validationResults.addError(
                instance = instance,
                propertyName = property.name,
                message = "has too few translations (minimum $minLangCount)"
            )
        }
    }

    if (acceptedLanguages != null) {
        val translationLanguages = translatedText.translations.keys
        val surplusLanguages = translationLanguages - acceptedLanguages

        if (surplusLanguages.any()) {
            validationResults.addError(
                instance = instance,
                propertyName = property.name,
                message = "contains translations with surplus languages ${surplusLanguages.map { it.iso6391Code }.sorted()}"
            )
        }
    }
}
