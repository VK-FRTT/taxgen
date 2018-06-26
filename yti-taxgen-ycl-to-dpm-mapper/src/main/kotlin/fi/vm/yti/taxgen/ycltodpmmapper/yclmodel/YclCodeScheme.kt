package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class YclCodeScheme(
    private val created: Instant?,
    private val modified: Instant?,

    private val codeValue: String?,
    private val defaultCode: YclDefaultCode,

    private val startDate: LocalDate?,
    private val endDate: LocalDate?,

    private val prefLabel: MutableMap<String, String>,
    private val definition: MutableMap<String, String>,
    private val description: MutableMap<String, String>
) {

    fun dpmDomainCode(): String {
        return codeValue!!
    }

    fun dpmDefaultMemberCode(): String {
        return defaultCode.codeValue!!
    }

    fun dpmConcept(): Concept {
        return Concept(
            createdAt = created!!,
            modifiedAt = modified!!,
            applicableFrom = startDate,
            applicableUntil = endDate,
            label = TranslatedText(prefLabel),
            description = TranslatedText(description)
        )
    }
}
