package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.ext.java.isBeforeOrEqualOrUndefined
import java.time.LocalDate

data class Concept(
    val owner: Owner,
    val createdAt: LocalDate,
    val modifiedAt: LocalDate?,
    val applicableFrom: LocalDate?,
    val applicableUntil: LocalDate?,
    val label: TranslatedText,
    val description: TranslatedText?
) {
    init {
        require(createdAt.isBeforeOrEqualOrUndefined(modifiedAt)) {
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
