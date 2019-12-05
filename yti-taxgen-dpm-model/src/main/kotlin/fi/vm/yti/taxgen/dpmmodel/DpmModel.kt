package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateElementValueUnique

data class DpmModel(
    val dictionaries: List<DpmDictionary>
) : Validatable {

    override fun validate(validationResults: ValidationResults) {

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "dictionaries",
            iterable = dictionaries,
            valueSelector = { it: DpmDictionary -> it.owner.prefix },
            valueDescription = "owner.prefix"
        )

        dictionaries.mapNotNull { it.explicitDomains + it.typedDomains }.flatten().let { allDomains ->
            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.domains",
                iterable = allDomains,
                valueSelector = { it: DpmElement -> it.uri },
                valueDescription = "Domain.uri"
            )

            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.domains",
                iterable = allDomains,
                valueSelector = { it: DpmElement -> it.code() },
                valueDescription = "Domain.code"
            )
        }

        dictionaries.mapNotNull { it.metricDomain?.metrics }.flatten().let { allMetrics ->
            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.metricDomain",
                iterable = allMetrics,
                valueSelector = { it: Metric -> it.uri },
                valueDescription = "Metric.uri"
            )

            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.metricDomain",
                iterable = allMetrics,
                valueSelector = { it: Metric -> it.metricCode },
                valueDescription = "Metric.metricCode"
            )
        }

        dictionaries.mapNotNull { it.metricDomain?.hierarchies }.flatten().let { allMetricHierarchies ->
            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.metricDomain",
                iterable = allMetricHierarchies,
                valueSelector = { it: Hierarchy -> it.uri },
                valueDescription = "Hierarchy.uri"
            )

            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.metricDomain",
                iterable = allMetricHierarchies,
                valueSelector = { it: Hierarchy -> it.hierarchyCode },
                valueDescription = "Hierarchy.hierarchyCode"
            )
        }
    }
}
