package fi.vm.yti.taxgen.ycltodpmmapper.extractor

import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.datapointmetamodel.OwnerConfig
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext

internal fun DpmDictionarySource.extractDpmDictionary(
    ctx: DpmMappingContext
): DpmDictionary {

    fun extractDpmOwner(): Owner {
        return ctx.extract(Owner.Companion) {
            val ownerConfig = ctx.deserializeJson<OwnerConfig>(dpmOwnerConfigData())

            ctx.diagnostic.updateCurrentTopicName(ownerConfig.name)

            val owner = Owner.fromConfig(ownerConfig, ctx.diagnostic)
            owner
        }
    }

    return ctx.extract(this) {
        val ownerSpecificCtx = ctx.cloneWithOwner(extractDpmOwner())
        ctx.diagnostic.updateCurrentTopicName(ownerSpecificCtx.owner.name)

        val explicitDomains = yclCodelistSources().map { codelistSource ->
            codelistSource.extractDpmExplicitDomain(ownerSpecificCtx)
        }

        val dpmDictionary = DpmDictionary(
            owner = ownerSpecificCtx.owner,
            explicitDomains = explicitDomains
        )

        dpmDictionary
    }
}
