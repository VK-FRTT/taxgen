package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext
import fi.vm.yti.taxgen.ycltodpmmapper.mappers.TranslatedTextMapper
import java.time.Instant
import java.time.LocalDate

data class YclCode(
    private val created: Instant?,
    private val modified: Instant?,

    private val codeValue: String?,

    private val startDate: LocalDate?,
    private val endDate: LocalDate?,

    private val order: Int?,

    private val prefLabel: Map<String, String>?,
    private val description: Map<String, String>?
) {

    fun mapToDpmMember(mappingContext: DpmMappingContext, defaultMemberCode: String): Member {

        val concept = Concept(
            createdAt = created!!, //TODO - proper error message
            modifiedAt = modified!!, //TODO - proper error message
            applicableFrom = startDate,
            applicableUntil = endDate,
            label = TranslatedTextMapper.fromYclLangText(prefLabel),
            description = TranslatedTextMapper.fromYclLangText(description),
            owner = mappingContext.owner
        )

        val memberCode = codeValue!! //TODO - proper error message

        return Member(
            concept = concept,
            memberCode = memberCode,
            defaultMember = (memberCode == defaultMemberCode)
        )
    }
}
