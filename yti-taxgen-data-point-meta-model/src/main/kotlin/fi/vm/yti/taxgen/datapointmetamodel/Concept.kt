package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.validateCondition
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateInstantLegalTimestamp
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateTranslatedText
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
) : Validatable {
    companion object {} //ktlint-disable no-empty-class-body

    init {
        label.defaultLanguage = owner.defaultLanguage
        description.defaultLanguage = owner.defaultLanguage
    }

    override fun validate(validationErrors: ValidationErrors) {

        validateInstantLegalTimestamp(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::createdAt
        )

        validateInstantLegalTimestamp(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::modifiedAt
        )

        validateCondition(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::modifiedAt,
            condition = { modifiedAt.isBefore(createdAt) },
            failMessage = { "is earlier than ${Concept::createdAt.name}" }
        )

        validateCondition(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::applicableUntil,
            condition = {
                (applicableUntil != null) && (applicableFrom != null) && applicableUntil.isBefore(
                    applicableFrom
                )
            },
            failMessage = { "is earlier than ${Concept::applicableFrom.name}" }
        )

        validateTranslatedText(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::label,
            minTranslationLength = 2,
            minLangCount = 1,
            acceptedLanguages = owner.languages
        )

        validateTranslatedText(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::description,
            minTranslationLength = 2,
            acceptedLanguages = owner.languages
        )
    }
}
