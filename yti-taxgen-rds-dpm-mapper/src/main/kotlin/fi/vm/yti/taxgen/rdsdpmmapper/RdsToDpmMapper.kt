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
import fi.vm.yti.taxgen.rdsdpmmapper.modelmapper.DpmDictionaryModelMapper
import fi.vm.yti.taxgen.rdsdpmmapper.modelmapper.DpmSourceModelMapper
import fi.vm.yti.taxgen.rdsprovider.DpmSource

class RdsToDpmMapper(
    private val diagnosticContext: DiagnosticContext
) {
    fun extractDpmModel(dpmSource: DpmSource): DpmModel {

        return diagnosticContext.withContext(
            contextType = DiagnosticContextType.RdsToDpmMapper,
            contextLabel = "RDS source data to DPM model"
        ) {
            val sourceModelMapper = DpmSourceModelMapper(
                dpmSource = dpmSource,
                diagnostic = diagnosticContext
            )

            val dictionaries = mutableListOf<DpmDictionary>()

            sourceModelMapper.eachDpmDictionaryModelMapper {
                val dictionary = mapAndValidateDpmDictionary(it)
                dictionaries.add(dictionary)
            }

            val dpmModel = DpmModel(
                dictionaries = dictionaries
            )

            diagnosticContext.validate(dpmModel)

            dpmModel
        }
    }

    private fun mapAndValidateDpmDictionary(
        dictionaryModelMapper: DpmDictionaryModelMapper
    ): DpmDictionary {
        lateinit var owner: Owner
        lateinit var explicitDomains: List<ExplicitDomain>
        lateinit var typedDomains: List<TypedDomain>
        lateinit var explicitDimensions: List<ExplicitDimension>
        lateinit var typedDimensions: List<TypedDimension>
        lateinit var metricDomains: List<MetricDomain>

        dictionaryModelMapper.dpmOwnerConfig {
            owner = mapAndValidateOwner(it, diagnosticContext)
        }

        dictionaryModelMapper.explicitDomainsAndHierarchiesCodeListModelMapper {
            explicitDomains = mapAndValidateExplicitDomainsAndHierarchies(it, owner, diagnosticContext)
        }

        dictionaryModelMapper.typedDomainsCodeListModelMapper {
            typedDomains = mapAndValidateTypedDomains(it, owner, diagnosticContext)
        }

        dictionaryModelMapper.explicitDimensionsCodeListModelMapper {
            explicitDimensions = mapAndValidateExplicitDimensions(it, owner, diagnosticContext)
        }

        dictionaryModelMapper.typedDimensionsCodeListModelMapper {
            typedDimensions = mapAndValidateTypedDimensions(it, owner, diagnosticContext)
        }

        dictionaryModelMapper.metricsCodeListModelMapper {
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
