package fi.vm.yti.taxgen.datapointmetamodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import kotlin.reflect.KProperty1

@Suppress("FINAL_UPPER_BOUND")
fun <I : Validatable, P : TranslatedText> validateTranslatedText(
    validationErrors: ValidationErrors,
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
            validationErrors.add(
                instance = instance,
                propertyName = property.name,
                message = "has too short translations for languages $shortTranslationLanguages"
            )
        }
    }

    if (minLangCount != null) {
        if (translatedText.translations.size < minLangCount) {
            validationErrors.add(
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
            validationErrors.add(
                instance = instance,
                propertyName = property.name,
                message = "contains translations with surplus languages ${surplusLanguages.map { it.iso6391Code }.sorted()}"
            )
        }
    }
}
