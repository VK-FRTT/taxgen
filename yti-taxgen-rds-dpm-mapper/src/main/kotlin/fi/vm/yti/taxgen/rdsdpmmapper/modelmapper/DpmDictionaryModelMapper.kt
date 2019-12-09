package fi.vm.yti.taxgen.rdsdpmmapper.modelmapper

import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource

internal class DpmDictionaryModelMapper(
    private val dpmDictionarySource: DpmDictionarySource,
    private val diagnostic: Diagnostic
) {
    fun dpmOwner(action: (Owner) -> Unit) {
        dpmDictionarySource.dpmOwner { owner ->

            diagnostic.updateCurrentContextDetails(
                contextTitle = owner.name
            )

            action(owner)
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
            contextTitle = reader.codeListMeta().diagnosticContextTitleFromLabel(
                diagnostic.diagnosticSourceLanguages()
            )
        )

        return reader
    }
}
