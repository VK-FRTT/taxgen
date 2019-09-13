package fi.vm.yti.taxgen.rdsdpmmapper.modelmapper

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.config.OwnerConfig

internal class DpmDictionaryModelMapper(
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

    fun metricsCodeListModelMapper(action: (CodeListModelMapper?) -> Unit) {
        dpmDictionarySource.metricsSource {
            action(codeListModelMapperForSource(it))
        }
    }

    fun explicitDomainsAndHierarchiesCodeListModelMapper(action: (CodeListModelMapper?) -> Unit) {
        dpmDictionarySource.explicitDomainsAndHierarchiesSource {
            action(codeListModelMapperForSource(it))
        }
    }

    fun explicitDimensionsCodeListModelMapper(action: (CodeListModelMapper?) -> Unit) {
        dpmDictionarySource.explicitDimensionsSource {
            action(codeListModelMapperForSource(it))
        }
    }

    fun typedDomainsCodeListModelMapper(action: (CodeListModelMapper?) -> Unit) {
        dpmDictionarySource.typedDomainsSource {
            action(codeListModelMapperForSource(it))
        }
    }

    fun typedDimensionsCodeListModelMapper(action: (CodeListModelMapper?) -> Unit) {
        dpmDictionarySource.typedDimensionsSource {
            action(codeListModelMapperForSource(it))
        }
    }

    private fun codeListModelMapperForSource(
        codeListSource: CodeListSource?
    ): CodeListModelMapper? {
        if (codeListSource == null) {
            return null
        }

        val reader = CodeListModelMapper(codeListSource, diagnostic)

        diagnostic.updateCurrentContextDetails(
            label = reader.codeListMeta().diagnosticLabel()
        )

        return reader
    }
}
