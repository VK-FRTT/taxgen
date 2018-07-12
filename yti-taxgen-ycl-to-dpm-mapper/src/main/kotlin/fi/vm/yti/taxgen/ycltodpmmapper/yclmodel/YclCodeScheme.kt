package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext
import fi.vm.yti.taxgen.ycltodpmmapper.mappers.TranslatedTextMapper
import java.time.Instant
import java.time.LocalDate

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
        return codeValue!! //TODO - proper error message
    }

    fun dpmDefaultMemberCode(): String {
        return defaultCode.codeValue!! //TODO - proper error message
    }

    fun mapToDpmConcept(mappingContext: DpmMappingContext): Concept {
        return Concept(
            createdAt = created!!, //TODO - proper error message
            modifiedAt = modified!!, //TODO - proper error message
            applicableFrom = startDate,
            applicableUntil = endDate,
            label = TranslatedTextMapper.fromYclLangText(prefLabel),
            description = TranslatedTextMapper.fromYclLangText(description),
            owner = mappingContext.owner
        )
    }
}
