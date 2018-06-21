package fi.vm.yti.taxgen.ycltodpmmapper.mapping

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.yclsourceprovider.CodeList
import fi.vm.yti.taxgen.ycltodpmmapper.model.YclCodeScheme
import fi.vm.yti.taxgen.ycltodpmmapper.model.YclCodesCollection

fun explicitDomainFromCodeList(codeList: CodeList): ExplicitDomain {
    val mapper = JacksonObjectMapper.lenientObjectMapper()

    val yclCodeScheme: YclCodeScheme = mapper.readValue(codeList.codeListData())
    val concept = conceptFromYclCodeScheme(yclCodeScheme)
    val domainCode = yclCodeScheme.dpmDomainCode()
    val defaultMemberCode = yclCodeScheme.dpmDefaultMemberCode()

    val explicitDomainMembers =
        codeList
            .codePagesData()
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