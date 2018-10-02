package fi.vm.yti.taxgen.ycltodpmmapper.extractor

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.config.OwnerConfig
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext

internal fun DpmDictionarySource.extractDpmDictionary(
    ctx: DpmMappingContext
): DpmDictionary {
    return ctx.extract(this) {
        val ownerSpecificCtx = ctx.cloneWithOwner(extractDpmOwner(dpmOwnerConfigData(), ctx))
        ctx.diagnostic.updateCurrentContextDetails(
            label = ownerSpecificCtx.owner.name
        )

        val explicitDomains = yclCodelistSources().map { it.extractDpmExplicitDomain(ownerSpecificCtx) }

        val dpmDictionary = DpmDictionary(
            owner = ownerSpecificCtx.owner,
            explicitDomains = explicitDomains
        )

        dpmDictionary
    }
}

private fun extractDpmOwner(dpmOwnerConfigData: String, ctx: DpmMappingContext): Owner {
    return ctx.extract(Owner.Companion) {
        val ownerConfig = JsonOps.readValue<OwnerConfig>(dpmOwnerConfigData, ctx.diagnostic)

        ctx.diagnostic.updateCurrentContextDetails(
            label = ownerConfig.name
        )

        val owner = Owner.fromConfig(ownerConfig, ctx.diagnostic)
        owner
    }
}
