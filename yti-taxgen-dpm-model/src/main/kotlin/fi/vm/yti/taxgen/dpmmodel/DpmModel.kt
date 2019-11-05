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

        dictionaries.mapNotNull { it.metricDomain?.hierarchies }.flatten().let { allHierarchies ->
            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.metricDomain",
                iterable = allHierarchies,
                valueSelector = { it: Hierarchy -> it.uri },
                valueDescription = "Hierarchy.uri"
            )

            validateElementValueUnique(
                validationResults = validationResults,
                instance = this,
                instancePropertyName = "dictionaries.metricDomain",
                iterable = allHierarchies,
                valueSelector = { it: Hierarchy -> it.hierarchyCode },
                valueDescription = "Hierarchy.hierarchyCode"
            )
        }
    }

    //TODO - validate that Domain codes unique within all Dictionaries
}
