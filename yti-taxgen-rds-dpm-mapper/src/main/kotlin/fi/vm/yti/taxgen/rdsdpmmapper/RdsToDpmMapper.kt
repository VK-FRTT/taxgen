package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateExplicitDomainsAndHierarchies
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateMetrics
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateOwner
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateTypedDomains
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.DpmDictionarySourceReader
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.DpmSourceReader
import fi.vm.yti.taxgen.rdsprovider.SourceProvider

class RdsToDpmMapper(
    private val diagnosticContext: DiagnosticContext
) {
    fun extractDpmDictionariesFromSource(
        sourceProvider: SourceProvider
    ): List<DpmDictionary> {

        return diagnosticContext.withContext(
            contextType = DiagnosticContextType.MapRdsToDpm,
            contextLabel = "RDS source data to DPM model"
        ) {
            val dictionaries = mutableListOf<DpmDictionary>()

            sourceProvider.withDpmSource { dpmSource ->
                val reader = DpmSourceReader(dpmSource, diagnosticContext)

                reader.eachDpmDictionarySource {
                    val dictionary = mapAndValidateDpmDictionary(it)
                    dictionaries.add(dictionary)
                }
            }

            dictionaries
        }
    }

    private fun mapAndValidateDpmDictionary(
        dpmDictionarySource: DpmDictionarySourceReader
    ): DpmDictionary {
        lateinit var owner: Owner
        lateinit var metrics: List<Metric>
        lateinit var explicitDomains: List<ExplicitDomain>
        lateinit var typedDomains: List<TypedDomain>

        dpmDictionarySource.dpmOwnerConfig {
            owner = mapAndValidateOwner(it, diagnosticContext)
        }

        dpmDictionarySource.metricsSource {
            metrics = mapAndValidateMetrics(it, owner, diagnosticContext)
        }

        dpmDictionarySource.explicitDomainsAndHierarchiesSource {
            explicitDomains = mapAndValidateExplicitDomainsAndHierarchies(it, owner, diagnosticContext)
        }

        dpmDictionarySource.typedDomainsSource {
            typedDomains = mapAndValidateTypedDomains(it, owner, diagnosticContext)
        }

        val dpmDictionary = DpmDictionary(
            owner = owner,
            metrics = metrics,
            explicitDomains = explicitDomains,
            typedDomains = typedDomains,
            explicitDimensions = emptyList(),
            typedDimensions = emptyList()
        )

        diagnosticContext.validate(dpmDictionary)

        return dpmDictionary
    }
}
