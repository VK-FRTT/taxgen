package fi.vm.yti.taxgen.ycltodpmmapper.extractor

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.config.YclCodelistSourceConfig
import fi.vm.yti.taxgen.yclsourceprovider.config.input.YclCodelistSourceConfigInput
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodeScheme
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodesCollection

internal fun YclCodelistSource.extractDpmExplicitDomain(
    ctx: DpmMappingContext
): ExplicitDomain {
    return ctx.extract(this) {

        val codelistConfig = extractCodelistSourceConfig(ctx)

        ctx.diagnostic.updateCurrentContextName(codelistConfig.domainCode) //TODO codelistConfig.uri to REF

        val codeScheme = extractCodeScheme(ctx)
        val members = extractMembers(ctx, codelistConfig, codeScheme)
        val hierarchies = extractHierarchies(ctx)

        ExplicitDomain(
            id = codeScheme.idOrEmpty(),
            uri = codeScheme.uriOrEmpty(),
            concept = codeScheme.dpmConcept(ctx.owner),
            domainCode = codelistConfig.domainCode,
            members = members,
            hierarchies = hierarchies
        )
    }
}

private fun YclCodelistSource.extractCodelistSourceConfig(ctx: DpmMappingContext): YclCodelistSourceConfig {
    return JsonOps.readValue<YclCodelistSourceConfigInput>(
        yclCodelistSourceConfigData(),
        ctx.diagnostic
    ).toValidConfig(ctx.diagnostic)
}

private fun YclCodelistSource.extractCodeScheme(ctx: DpmMappingContext): YclCodeScheme {
    return JsonOps.readValue(yclCodeSchemeData(), ctx.diagnostic)
}

private fun YclCodelistSource.extractMembers(
    ctx: DpmMappingContext,
    codelistConfig: YclCodelistSourceConfig,
    codeScheme: YclCodeScheme
): List<Member> {
    return yclCodePagesData()
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
                    memberCode = yclCode.asMemberCode(codelistConfig.memberCodePrefix),
                    defaultMember = yclCode.isDefaultCode(codeScheme.defaultCode)
                )
            }
        }
        .toList()
}

private fun YclCodelistSource.extractHierarchies(ctx: DpmMappingContext) =
    yclCodelistExtensionSources().mapNotNull { it.tryExtractDpmHierarchy(ctx) }
