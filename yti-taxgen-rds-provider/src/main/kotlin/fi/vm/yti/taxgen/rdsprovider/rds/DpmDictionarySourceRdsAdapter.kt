package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.config.DpmDictionarySourceConfig

internal class DpmDictionarySourceRdsAdapter(
    private val config: DpmDictionarySourceConfig,
    private val diagnostic: Diagnostic
) : DpmDictionarySource {

    override fun dpmOwnerConfigData(): String {
        return JsonOps.writeAsJsonString(config.owner)
    }

    override fun metricsSource(): CodeListSource? {
        return codeListSourceOrNullForUri(
            config.metrics.uri,
            CodeListBlueprint.metrics()
        )
    }

    override fun explicitDomainsAndHierarchiesSource(): CodeListSource? {
        return codeListSourceOrNullForUri(
            config.explicitDomainsAndHierarchies.uri,
            CodeListBlueprint.explicitDomainsAndHierarchies()
        )
    }

    override fun explicitDimensionsSource(): CodeListSource? {
        return codeListSourceOrNullForUri(
            config.explicitDimensions.uri,
            CodeListBlueprint.explicitOrTypedDimensions()
        )
    }

    override fun typedDomainsSource(): CodeListSource? {
        return codeListSourceOrNullForUri(
            config.typedDomains.uri,
            CodeListBlueprint.typedDomains()
        )
    }

    override fun typedDimensionsSource(): CodeListSource? {
        return codeListSourceOrNullForUri(
            config.typedDimensions.uri,
            CodeListBlueprint.explicitOrTypedDimensions()
        )
    }

    private fun codeListSourceOrNullForUri(
        uri: String?,
        blueprint: CodeListBlueprint
    ): CodeListSource? {
        return if (uri != null) {
            CodeListSourceRdsAdapter(
                rdsCodeListUri = uri,
                blueprint = blueprint,
                diagnostic = diagnostic
            )
        } else {
            null
        }
    }
}
