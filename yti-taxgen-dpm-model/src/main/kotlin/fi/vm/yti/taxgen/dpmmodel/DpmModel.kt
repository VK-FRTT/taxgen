package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterableDpmElementsValueUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterableValuesUnique

data class DpmModel(
    val dictionaries: List<DpmDictionary>
) : Validatable {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateIterableValuesUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = dictionaries,
            valueSelector = { it: DpmDictionary -> it.owner.prefix },
            valueName = listOf(Owner::class, Owner::prefix)
        )

        dictionaries.mapNotNull { it.explicitDomains + it.typedDomains }.flatten().let { allDomains ->
            validateIterableDpmElementsValueUnique(
                validationResultBuilder = validationResultBuilder,
                iterable = allDomains,
                valueSelector = { it: DpmElement -> it.uri },
                valueName = ExplicitDomain::uri
            )

            validateIterableDpmElementsValueUnique(
                validationResultBuilder = validationResultBuilder,
                iterable = allDomains,
                valueSelector = { it: DpmElement -> it.code() },
                valueName = ExplicitDomain::domainCode
            )
        }

        dictionaries.mapNotNull { it.metricDomain?.metrics }.flatten().let { allMetrics ->
            validateIterableDpmElementsValueUnique(
                validationResultBuilder = validationResultBuilder,
                iterable = allMetrics,
                valueSelector = { it: Metric -> it.uri },
                valueName = Metric::uri
            )

            validateIterableDpmElementsValueUnique(
                validationResultBuilder = validationResultBuilder,
                iterable = allMetrics,
                valueSelector = { it: Metric -> it.metricCode },
                valueName = Metric::metricCode
            )
        }

        dictionaries.mapNotNull { it.metricDomain?.hierarchies }.flatten().let { allMetricHierarchies ->
            validateIterableDpmElementsValueUnique(
                validationResultBuilder = validationResultBuilder,
                iterable = allMetricHierarchies,
                valueSelector = { it: Hierarchy -> it.uri },
                valueName = Hierarchy::uri
            )

            validateIterableDpmElementsValueUnique(
                validationResultBuilder = validationResultBuilder,
                iterable = allMetricHierarchies,
                valueSelector = { it: Hierarchy -> it.hierarchyCode },
                valueName = Hierarchy::hierarchyCode
            )
        }

    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "DPM Model",
            subjectIdentifier = ""
        )
    }
}
