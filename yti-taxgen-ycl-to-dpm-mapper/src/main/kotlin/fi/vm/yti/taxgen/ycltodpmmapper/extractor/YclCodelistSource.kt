package fi.vm.yti.taxgen.ycltodpmmapper.extractor

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodeScheme
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodesCollection

internal fun YclCodelistSource.extractDpmExplicitDomain(
    ctx: DpmMappingContext
): ExplicitDomain {

    data class DomainDetails(val concept: Concept, val domainCode: String?, val defaultMemberCode: String?)

    fun extractDomainDetails(): DomainDetails {
        val codeScheme = ctx.deserializeJson<YclCodeScheme>(yclCodeschemeData())

        return DomainDetails(
            concept = Concept.fromYclCodeScheme(codeScheme, ctx.owner),
            domainCode = codeScheme.codeValue,
            defaultMemberCode = codeScheme.defaultCode?.codeValue
        )
    }

    fun isDefaultMember(memberCode: String?, defaultMemberCode: String?): Boolean {
        if (defaultMemberCode.isNullOrEmpty()) {
            return false
        }

        if (memberCode.isNullOrEmpty()) {
            return false
        }

        return memberCode == defaultMemberCode
    }

    fun extractMembers(defaultMemberCode: String?): List<Member> {
        val yclCodes = yclCodePagesData().asSequence()
            .map { data ->
                ctx.deserializeJson<YclCodesCollection>(data).results
                    ?: ctx.diagnostic.fatal("Missing YCL Codes")
            }
            .flatten()
            .toList()

        return yclCodes.map { yclCode ->
            ctx.extract(yclCode) {
                val concept = Concept.fromYclCode(yclCode, ctx.owner)

                Member(
                    concept = concept,
                    memberCode = yclCode.codeValue ?: "",
                    defaultMember = isDefaultMember(yclCode.codeValue, defaultMemberCode)
                )
            }
        }
    }

    return ctx.extract(this) {

        val domainDetails = extractDomainDetails()
        ctx.diagnostic.updateCurrentTopicName(domainDetails.domainCode)

        val members = extractMembers(domainDetails.defaultMemberCode)

        ExplicitDomain(
            concept = domainDetails.concept,
            domainCode = domainDetails.domainCode ?: "",
            members = members
        )
    }
}
