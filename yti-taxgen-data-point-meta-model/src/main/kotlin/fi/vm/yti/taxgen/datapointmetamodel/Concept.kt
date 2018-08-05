package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.ext.java.isBeforeOrEqual
import fi.vm.yti.taxgen.commons.ext.java.isBeforeOrEqualOrUndefined
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

        //TODO - validate timestamps not Instant.EPOCH,

        //TODO - validateData label having only those languages which Owner defines
        //TODO - Then validateData that there is label at least with one lang

        require(createdAt.isBeforeOrEqual(modifiedAt)) {
            "createdAt must precede modifiedAt"
        }

        //TODO applicableFrom && applicableUntil validation logic??
        if (applicableFrom != null) {
            require(applicableFrom.isBeforeOrEqualOrUndefined(applicableUntil)) {
                "applicableFrom must precede applicableUntil"
            }
        }

        require(!label.translations.isEmpty()) {
            "label must contain at least one translation"
        }
    }
}
