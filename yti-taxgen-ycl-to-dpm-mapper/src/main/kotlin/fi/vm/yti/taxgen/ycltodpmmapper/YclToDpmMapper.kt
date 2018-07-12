package fi.vm.yti.taxgen.ycltodpmmapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.datapointmetamodel.OwnerConfig
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodeScheme
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodesCollection

class YclToDpmMapper {

    fun mapDpmDictionariesFromSource(yclSource: YclSource): List<DpmDictionary> {
        val objectMapper = JacksonObjectMapper.lenientObjectMapper()

        val dpmDictionarySources = yclSource.dpmDictionarySources()
        val dpmDictionaries = dpmDictionarySources.map { mapDpmDictionaryFromSource(objectMapper, it) }

        return dpmDictionaries
    }

    private fun mapDpmDictionaryFromSource(
        objectMapper: ObjectMapper,
        dictionarySource: DpmDictionarySource
    ): DpmDictionary {

        val owner = mapOwnerFromSource(objectMapper, dictionarySource)

        val mappingContext = DpmMappingContext(
            owner,
            objectMapper
        )

        val yclCodelistSources = dictionarySource.yclCodelistSources()
        val explicitDomains = yclCodelistSources.map { mapExplicitDomainFromSource(mappingContext, it) }

        return DpmDictionary(
            owner = owner,
            explicitDomains = explicitDomains
        )
    }

    private fun mapOwnerFromSource(
        objectMapper: ObjectMapper,
        dictionarySource: DpmDictionarySource
    ): Owner {
        val ownerConfig = objectMapper.readValue<OwnerConfig>(dictionarySource.dpmOwnerConfigData())
        return ownerConfig.toOwner()
    }

    private fun mapExplicitDomainFromSource(
        mappingContext: DpmMappingContext,
        codelistSource: YclCodelistSource
    ): ExplicitDomain {

        val yclCodeScheme = mappingContext.objectMapper.readValue<YclCodeScheme>(codelistSource.yclCodeschemeData())

        val yclCodes = codelistSource.yclCodePagesData().asSequence()
            .map { mappingContext.objectMapper.readValue<YclCodesCollection>(it).results }
            .flatten()
            .toList()

        val defaultMemberCode = yclCodeScheme.dpmDefaultMemberCode()
        val members = yclCodes.map { it.mapToDpmMember(mappingContext, defaultMemberCode) }

        val concept = yclCodeScheme.mapToDpmConcept(mappingContext)
        val domainCode = yclCodeScheme.dpmDomainCode()

        return ExplicitDomain(
            concept = concept,
            domainCode = domainCode,
            members = members
        )
    }
}
