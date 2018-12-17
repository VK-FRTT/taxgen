package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
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
        return codeListSourceForUri(config.metrics.uri)
    }

    override fun explicitDomainsAndHierarchiesSource(): CodeListSource? {
        return codeListSourceForUri(config.explicitDomainsAndHierarchies.uri)
    }

    override fun explicitDimensionsSource(): CodeListSource? {
        return codeListSourceForUri(config.explicitDimensions.uri)
    }

    override fun typedDomainsSource(): CodeListSource? {
        return codeListSourceForUri(config.typedDomains.uri)
    }

    override fun typedDimensionsSource(): CodeListSource? {
        return codeListSourceForUri(config.typedDimensions.uri)
    }

    private fun codeListSourceForUri(uri: String?): CodeListSource? {
        return if (uri != null) {
            CodeListSourceRdsAdapter(diagnostic, uri)
        } else {
            null
        }
    }
}
