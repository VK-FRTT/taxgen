package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateTimestamp
import fi.vm.yti.taxgen.dpmmodel.validators.validateTranslatedText
import java.time.Instant
import java.time.LocalDate

data class Concept(
    val createdAt: Instant,
    val modifiedAt: Instant,
    val applicableFrom: LocalDate?,
    val applicableUntil: LocalDate?,
    val label: TranslatedText,
    val description: TranslatedText,
    val owner: Owner
) {
    companion object {

        fun empty(owner: Owner) =
            Concept(
                createdAt = Instant.now(),
                modifiedAt = Instant.now(),
                applicableFrom = null,
                applicableUntil = null,
                label = TranslatedText.empty(),
                description = TranslatedText.empty(),
                owner = owner
            )
    }

    init {
        label.defaultLanguage = owner.defaultLanguage
        description.defaultLanguage = owner.defaultLanguage
    }

    fun validateConcept(
        validationResults: ValidationResults,
        minLabelLangCount: Int
    ) {

        validateTimestamp(
            validationResults = validationResults,
            instance = this,
            property = Concept::createdAt
        )

        validateTimestamp(
            validationResults = validationResults,
            instance = this,
            property = Concept::modifiedAt
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Concept::modifiedAt,
            condition = { !modifiedAt.isBefore(createdAt) },
            message = { "is earlier than ${Concept::createdAt.name}" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Concept::applicableUntil,
            condition = condition@{
                if (applicableUntil == null) return@condition true
                if (applicableFrom == null) return@condition true

                !applicableUntil.isBefore(applicableFrom)
            },
            message = { "is earlier than ${Concept::applicableFrom.name}" }
        )

        validateTranslatedText(
            validationResults = validationResults,
            instance = this,
            property = Concept::label,
            minTranslationLength = 2,
            minLangCount = minLabelLangCount,
            acceptedLanguages = owner.languages
        )

        validateTranslatedText(
            validationResults = validationResults,
            instance = this,
            property = Concept::description,
            minTranslationLength = 2,
            acceptedLanguages = owner.languages
        )
    }
}
