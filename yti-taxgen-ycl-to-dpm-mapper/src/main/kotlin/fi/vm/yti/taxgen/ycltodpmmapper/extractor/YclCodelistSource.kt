package fi.vm.yti.taxgen.ycltodpmmapper.extractor

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.config.YclCodelistSourceConfig
import fi.vm.yti.taxgen.yclsourceprovider.config.input.YclCodelistSourceConfigInput
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCode
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodeScheme
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodesCollection

internal fun YclCodelistSource.extractDpmExplicitDomain(
    ctx: DpmMappingContext
): ExplicitDomain {

    data class DomainDetails(
        val id: String,
        val codelistSourceConfig: YclCodelistSourceConfig,
        val concept: Concept,
        val defaultMemberCode: String?
    )

    fun extractDomainDetails(): DomainDetails {

        val codelistConfigInput =
            JsonOps.readValue<YclCodelistSourceConfigInput>(yclCodelistSourceConfigData(), ctx.diagnostic)

        val codeScheme = JsonOps.readValue<YclCodeScheme>(yclCodeSchemeData(), ctx.diagnostic)

        return DomainDetails(
            id = codeScheme.id ?: "",
            codelistSourceConfig = codelistConfigInput.toValidConfig(ctx.diagnostic),
            concept = Concept.fromYclCodeScheme(codeScheme, ctx.owner),
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

    fun memberCode(domainDetails: DomainDetails, yclCode: YclCode) =
        "${domainDetails.codelistSourceConfig.memberCodePrefix ?: ""}${yclCode.codeValue ?: ""}"

    fun extractMembers(domainDetails: DomainDetails): List<Member> {
        val yclCodes = yclCodePagesData()
            .map { data ->
                val codesCollection = JsonOps.readValue<YclCodesCollection>(data, ctx.diagnostic)
                codesCollection.results ?: ctx.diagnostic.fatal("Missing YCL Codes")
            }
            .flatten()
            .toList()

        return yclCodes.map { yclCode ->
            ctx.extract(yclCode) {
                val concept = Concept.fromYclCode(yclCode, ctx.owner)

                Member(
                    id = yclCode.id ?: "",
                    concept = concept,
                    memberCode = memberCode(domainDetails, yclCode),
                    defaultMember = isDefaultMember(yclCode.codeValue, domainDetails.defaultMemberCode)
                )
            }
        }
    }

    return ctx.extract(this) {

        val domainDetails = extractDomainDetails()
        ctx.diagnostic.updateCurrentContextName(domainDetails.codelistSourceConfig.domainCode)

        val members = extractMembers(domainDetails)

        ExplicitDomain(
            id = domainDetails.id,
            concept = domainDetails.concept,
            domainCode = domainDetails.codelistSourceConfig.domainCode,
            members = members,
            hierarchies = listOf()
        )
    }
}
