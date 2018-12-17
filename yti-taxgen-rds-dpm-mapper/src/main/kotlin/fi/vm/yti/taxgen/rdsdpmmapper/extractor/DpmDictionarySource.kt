package fi.vm.yti.taxgen.rdsdpmmapper.extractor

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.config.OwnerConfig
import fi.vm.yti.taxgen.rdsdpmmapper.DpmMappingContext

internal fun DpmDictionarySource.extractDpmDictionary(
    ctx: DpmMappingContext
): DpmDictionary {
    return ctx.extract(this) {
        val ownerSpecificCtx = ctx.cloneWithOwner(extractDpmOwner(dpmOwnerConfigData(), ctx))
        ctx.diagnostic.updateCurrentContextDetails(
            label = ownerSpecificCtx.owner.name
        )

        val dpmDictionary = DpmDictionary(
            owner = ownerSpecificCtx.owner,
            explicitDomains = emptyList()
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
