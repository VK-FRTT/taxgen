package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateTimestamp
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

        validateTimestamp(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::createdAt
        )

        validateTimestamp(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::modifiedAt
        )

        validateConditionTruthy(
            validationErrors = validationErrors,
            instance = this,
            property = Concept::modifiedAt,
            condition = { !modifiedAt.isBefore(createdAt) },
            message = { "is earlier than ${Concept::createdAt.name}" }
        )

        validateConditionTruthy(
            validationErrors = validationErrors,
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

    fun diagnosticLabel(): String {
        val defaultLabel = label.defaultTranslation()
        if (defaultLabel != null && defaultLabel.isNotBlank()) return defaultLabel

        val anyLablel = label.anyTranslation() //TODO - anyTranslation => anyNonEmptyTranslation
        if (anyLablel != null && anyLablel.second.isNotBlank()) return "${anyLablel.second} (${anyLablel.first.iso6391Code})"

        return ""
    }
}
