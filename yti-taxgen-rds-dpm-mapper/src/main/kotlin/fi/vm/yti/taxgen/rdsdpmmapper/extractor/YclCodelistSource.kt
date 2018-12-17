package fi.vm.yti.taxgen.rdsdpmmapper.extractor

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsdpmmapper.DpmMappingContext
import fi.vm.yti.taxgen.rdsdpmmapper.yclmodel.YclCodeScheme
import fi.vm.yti.taxgen.rdsdpmmapper.yclmodel.YclCodesCollection

internal fun CodeListSource.extractDpmExplicitDomainWithHierarchies(
    ctx: DpmMappingContext
): ExplicitDomain {
    return ctx.extract(this) {
        ctx.diagnostic.updateCurrentContextDetails(
            identifier = "TODO"
        )

        val codeScheme = extractCodeScheme(ctx)
        val members = extractMembers(ctx, codeScheme)
        val hierarchies = extractHierarchies(ctx)

        ExplicitDomain(
            id = codeScheme.idOrEmpty(),
            uri = codeScheme.uriOrEmpty(),
            concept = codeScheme.dpmConcept(ctx.owner),
            domainCode = "TODO",
            members = members,
            hierarchies = emptyList()
        )
    }
}

private fun CodeListSource.extractCodeScheme(ctx: DpmMappingContext): YclCodeScheme {
    return JsonOps.readValue(codeListData(), ctx.diagnostic)
}

private fun CodeListSource.extractMembers(
    ctx: DpmMappingContext,
    codeScheme: YclCodeScheme
): List<Member> {
    return codePagesData()
        .map { codePageData ->
            JsonOps.readValue<YclCodesCollection>(codePageData, ctx.diagnostic)
                .results ?: ctx.diagnostic.fatal("Missing YCL Codes")
        }
        .flatten()
        .map { yclCode ->
            ctx.extract(yclCode) {
                Member(
                    id = yclCode.idOrEmpty(),
                    uri = yclCode.uriOrEmpty(),
                    concept = yclCode.dpmConcept(ctx.owner),
                    memberCode = yclCode.asMemberCode("TODO memberCodePrefix"),
                    defaultMember = yclCode.isDefaultCode(codeScheme.defaultCode)
                )
            }
        }
        .toList()
}

private fun CodeListSource.extractHierarchies(ctx: DpmMappingContext) =
    extensionSources().mapNotNull { it.tryExtractDpmHierarchy(ctx) }
