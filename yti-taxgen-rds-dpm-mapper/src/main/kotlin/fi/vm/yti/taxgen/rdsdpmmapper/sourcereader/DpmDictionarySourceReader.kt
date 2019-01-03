package fi.vm.yti.taxgen.rdsdpmmapper.sourcereader

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.config.OwnerConfig

internal class DpmDictionarySourceReader(
    private val dpmDictionarySource: DpmDictionarySource,
    private val diagnostic: Diagnostic
) {
    fun dpmOwnerConfig(action: (OwnerConfig) -> Unit) {

        dpmDictionarySource.dpmOwnerConfigData { data ->
            val ownerConfig = JsonOps.readValue<OwnerConfig>(data, diagnostic)

            diagnostic.updateCurrentContextDetails(
                label = ownerConfig.name
            )

            action(ownerConfig)
        }
    }

    fun metricsSource(action: (CodeListSourceReader?) -> Unit) {
        dpmDictionarySource.metricsSource {
            action(codeListSourceMapperForSource(it))
        }
    }

    fun explicitDomainsAndHierarchiesSource(action: (CodeListSourceReader?) -> Unit) {
        dpmDictionarySource.explicitDomainsAndHierarchiesSource {
            action(codeListSourceMapperForSource(it))
        }
    }

    fun explicitDimensionsSource(action: (CodeListSourceReader?) -> Unit) {
        dpmDictionarySource.explicitDimensionsSource {
            action(codeListSourceMapperForSource(it))
        }
    }

    fun typedDomainsSource(action: (CodeListSourceReader?) -> Unit) {
        dpmDictionarySource.typedDomainsSource {
            action(codeListSourceMapperForSource(it))
        }
    }

    fun typedDimensionsSource(action: (CodeListSourceReader?) -> Unit) {
        dpmDictionarySource.typedDimensionsSource {
            action(codeListSourceMapperForSource(it))
        }
    }

    private fun codeListSourceMapperForSource(
        codeListSource: CodeListSource?
    ): CodeListSourceReader? {
        if (codeListSource == null) {
            return null
        }

        return CodeListSourceReader(codeListSource, diagnostic)
    }
}
