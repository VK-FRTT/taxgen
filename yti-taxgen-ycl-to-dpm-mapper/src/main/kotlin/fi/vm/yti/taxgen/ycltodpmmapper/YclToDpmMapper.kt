package fi.vm.yti.taxgen.ycltodpmmapper

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodeScheme
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodesCollection

class YclToDpmMapper {

    fun dpmDictionariesFromYclSource(yclSource: YclSource): List<DpmDictionary> {
        val mapper = JacksonObjectMapper.lenientObjectMapper()

        return yclSource.dpmDictionarySources().map { dictionarySource ->

            val owner = mapper.readValue<Owner>(dictionarySource.dpmOwnerInfoData())

            val explicitDomains =
                dictionarySource
                    .yclCodelistSources()
                    .map { codelistSource -> explicitDomainFromCodeListSource(codelistSource) }

            DpmDictionary(
                owner = owner,
                explicitDomains = explicitDomains
            )
        }
    }

    private fun explicitDomainFromCodeListSource(codeList: YclCodelistSource): ExplicitDomain {
        val mapper = JacksonObjectMapper.lenientObjectMapper()

        val yclCodeScheme = mapper.readValue<YclCodeScheme>(codeList.yclCodeschemeData())
        val concept = yclCodeScheme.dpmConcept()
        val domainCode = yclCodeScheme.dpmDomainCode()
        val defaultMemberCode = yclCodeScheme.dpmDefaultMemberCode()

        val explicitDomainMembers =
            codeList
                .yclCodePagesData()
                .asSequence()
                .map { codePageData ->
                    val yclCodesCollection: YclCodesCollection = mapper.readValue(codePageData)
                    yclCodesCollection.results
                }
                .flatten()
                .map { yclCode ->
                    yclCode.dpmExplicitDomainMemberWithDefaultness(defaultMemberCode)
                }
                .toList()

        return ExplicitDomain(
            concept = concept,
            domainCode = domainCode,
            members = explicitDomainMembers
        )
    }
}
