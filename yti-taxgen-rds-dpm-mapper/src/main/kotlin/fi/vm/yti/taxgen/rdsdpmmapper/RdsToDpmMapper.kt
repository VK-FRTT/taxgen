package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateExplicitDomainsAndHierarchies
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateMetrics
import fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper.mapAndValidateOwner
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.DpmDictionarySourceReader
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.DpmSourceReader
import fi.vm.yti.taxgen.rdsprovider.DpmSource

class RdsToDpmMapper(
    private val diagnostic: Diagnostic
) {
    fun extractDpmDictionariesFromSource(
        dpmSource: DpmSource
    ): List<DpmDictionary> {

        return diagnostic.withContext(
            contextType = DiagnosticContextType.MappingRdsToDpm,
            contextLabel = "RDS source data to DPM model"
        ) {
            val dictionaries = mutableListOf<DpmDictionary>()

            val reader = DpmSourceReader(dpmSource, diagnostic)

            reader.eachDpmDictionarySource {
                val dictionary = mapAndValidateDpmDictionary(it)
                dictionaries.add(dictionary)
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

        dpmDictionarySource.dpmOwnerConfig {
            owner = mapAndValidateOwner(it, diagnostic)
        }

        dpmDictionarySource.metricsSource {
            metrics = mapAndValidateMetrics(it, owner, diagnostic)
        }

        dpmDictionarySource.explicitDomainsAndHierarchiesSource {
            explicitDomains = mapAndValidateExplicitDomainsAndHierarchies(it, owner, diagnostic)
        }

        val dpmDictionary = DpmDictionary(
            owner = owner,
            metrics = metrics,
            explicitDomains = explicitDomains,
            typedDomains = emptyList(),
            explicitDimensions = emptyList(),
            typedDimensions = emptyList()
        )

        diagnostic.validate(dpmDictionary)

        return dpmDictionary
    }
}
