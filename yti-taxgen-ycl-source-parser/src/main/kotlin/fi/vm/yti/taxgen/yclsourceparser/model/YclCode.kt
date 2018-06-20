package fi.vm.yti.taxgen.yclsourceparser.model

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomainMember
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import java.time.Instant
import java.time.LocalDate

data class YclCode(
    private val created: Instant?,
    private val modified: Instant?,

    private val codeValue: String?,

    private val startDate: LocalDate?,
    private val endDate: LocalDate?,

    private val prefLabel: Map<String, String>?,
    private val description: Map<String, String>?
) {

    fun dpmExplicitDomainMemberWithDefaultness(defaultMemberCode: String): ExplicitDomainMember {

        val concept = Concept(
            createdAt = created!!, //TODO - report errors if mandatory fields are missing in YCL data
            modifiedAt = modified!!,
            applicableFrom = startDate,
            applicableUntil = endDate,
            label = TranslatedText(prefLabel!!),
            description = TranslatedText(description!!)
        )

        val memberCode = codeValue!!

        return ExplicitDomainMember(
            concept = concept,
            memberCode = memberCode,
            defaultMember = (memberCode == defaultMemberCode)
        )
    }
}
