package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateExplicitDimensions
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateExplicitDomainsAndHierarchies
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateMetricDomain
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateOwner
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateTypedDimensions
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateTypedDomains
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.DpmDictionarySourceReader
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.DpmSourceReader
import fi.vm.yti.taxgen.rdsprovider.SourceProvider

class RdsToDpmMapper(
    private val diagnosticContext: DiagnosticContext
) {
    fun extractDpmModelFromSource(
        sourceProvider: SourceProvider
    ): DpmModel {

        return diagnosticContext.withContext(
            contextType = DiagnosticContextType.RdsToDpmMapper,
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

            val model = DpmModel(
                dictionaries = dictionaries
            )

            diagnosticContext.validate(model)

            model
        }
    }

    private fun mapAndValidateDpmDictionary(
        dpmDictionarySource: DpmDictionarySourceReader
    ): DpmDictionary {
        lateinit var owner: Owner
        lateinit var explicitDomains: List<ExplicitDomain>
        lateinit var typedDomains: List<TypedDomain>
        lateinit var explicitDimensions: List<ExplicitDimension>
        lateinit var typedDimensions: List<TypedDimension>
        lateinit var metricDomains: List<MetricDomain>

        dpmDictionarySource.dpmOwnerConfig {
            owner = mapAndValidateOwner(it, diagnosticContext)
        }

        dpmDictionarySource.explicitDomainsAndHierarchiesSource {
            explicitDomains = mapAndValidateExplicitDomainsAndHierarchies(it, owner, diagnosticContext)
        }

        dpmDictionarySource.typedDomainsSource {
            typedDomains = mapAndValidateTypedDomains(it, owner, diagnosticContext)
        }

        dpmDictionarySource.explicitDimensionsSource {
            explicitDimensions = mapAndValidateExplicitDimensions(it, owner, diagnosticContext)
        }

        dpmDictionarySource.typedDimensionsSource {
            typedDimensions = mapAndValidateTypedDimensions(it, owner, diagnosticContext)
        }

        dpmDictionarySource.metricsSource {
            metricDomains = mapAndValidateMetricDomain(it, owner, diagnosticContext)
        }

        val dpmDictionary = DpmDictionary(
            owner = owner,
            metricDomains = metricDomains,
            explicitDomains = explicitDomains,
            typedDomains = typedDomains,
            explicitDimensions = explicitDimensions,
            typedDimensions = typedDimensions
        )

        diagnosticContext.validate(dpmDictionary)

        return dpmDictionary
    }
}
