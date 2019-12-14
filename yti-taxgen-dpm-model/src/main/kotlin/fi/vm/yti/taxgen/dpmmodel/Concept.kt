package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropTimestamp
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropTranslatedText
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

    fun validateConcept(
        validationResultBuilder: ValidationResultBuilder,
        minLabelLangCount: Int
    ) {
        validatePropTimestamp(
            validationResultBuilder = validationResultBuilder,
            property = this::createdAt
        )

        validatePropTimestamp(
            validationResultBuilder = validationResultBuilder,
            property = this::modifiedAt
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::modifiedAt,
            condition = { it.isSameOrAfter(createdAt) },
            reason = { "Is earlier than ${Concept::createdAt.name.capitalize()}" }
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::applicableUntil,
            condition = condition@{
                it ?: return@condition true
                applicableFrom ?: return@condition true

                it.isSameOrAfter(applicableFrom)
            },
            reason = { "Is earlier than ${Concept::applicableFrom.name.capitalize()}" }
        )

        validatePropTranslatedText(
            validationResultBuilder = validationResultBuilder,
            property = this::label,
            minTranslationLength = 2,
            minLangCount = minLabelLangCount,
            acceptedLanguages = owner.languages
        )

        validatePropTranslatedText(
            validationResultBuilder = validationResultBuilder,
            property = this::description,
            minTranslationLength = 2,
            acceptedLanguages = owner.languages
        )
    }

    private fun Instant.isSameOrAfter(other: Instant): Boolean {
        return compareTo(other) >= 0
    }

    private fun LocalDate.isSameOrAfter(other: LocalDate): Boolean {
        return compareTo(other) >= 0
    }
}
