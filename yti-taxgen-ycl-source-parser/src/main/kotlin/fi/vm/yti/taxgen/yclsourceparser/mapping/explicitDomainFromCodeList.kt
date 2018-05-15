package fi.vm.yti.taxgen.yclsourceparser.mapping

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.yclsourceparser.model.CodeScheme
import fi.vm.yti.taxgen.yclsourceparser.model.CodesCollection
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.JacksonObjectMapper

fun explicitDomainFromCodeList(codeList: CodeList): ExplicitDomain {
    val mapper = JacksonObjectMapper.lenientObjectMapper()

    val codeScheme: CodeScheme = mapper.readValue(codeList.codeScheme())
    val codesCollection: CodesCollection = mapper.readValue(codeList.codes())

    val concept = conceptFromCodeScheme(codeScheme)
    val domainCode = domainCodeFromCodeScheme(codeScheme)
    val explicitDomainMembers = codesCollection.results.map { code -> explicitDomainMemberFromCode(code) }

    return ExplicitDomain(
        concept = concept,
        domainCode = domainCode,
        members = explicitDomainMembers
    )
}
