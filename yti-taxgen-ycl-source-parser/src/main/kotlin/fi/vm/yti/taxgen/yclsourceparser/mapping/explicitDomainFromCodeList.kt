package fi.vm.yti.taxgen.yclsourceparser.mapping

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.yclsourceparser.model.YclCodeScheme
import fi.vm.yti.taxgen.yclsourceparser.model.YclCodesCollection
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.JacksonObjectMapper

fun explicitDomainFromCodeList(codeList: CodeList): ExplicitDomain {
    val mapper = JacksonObjectMapper.lenientObjectMapper()

    val yclCodeScheme: YclCodeScheme = mapper.readValue(codeList.codeListData())
    val concept = conceptFromYclCodeScheme(yclCodeScheme)
    val domainCode = domainCodeFromYclCodeScheme(yclCodeScheme)

    val explicitDomainMembers =
        codeList.codePagesData()
            .asSequence()
            .map { codePageData ->
                val yclCodesCollection: YclCodesCollection = mapper.readValue(codePageData)
                yclCodesCollection.results
            }
            .flatten()
            .map { yclCode ->
                explicitDomainMemberFromYclCode(yclCode)
            }
            .toList()

    return ExplicitDomain(
        concept = concept,
        domainCode = domainCode,
        members = explicitDomainMembers
    )
}
