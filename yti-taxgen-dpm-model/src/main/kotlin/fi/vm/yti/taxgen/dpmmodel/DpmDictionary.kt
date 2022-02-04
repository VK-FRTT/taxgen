package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmElementCrossReferences
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterableDpmElementsValueUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropsLengths

data class DpmDictionary(
    val owner: Owner,
    val explicitDomains: List<ExplicitDomain>,
    val typedDomains: List<TypedDomain>,
    val explicitDimensions: List<ExplicitDimension>,
    val typedDimensions: List<TypedDimension>,
    val metricDomain: MetricDomain?
) : Validatable {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validatePropsLengths(
            validationResultBuilder = validationResultBuilder,
            properties = listOf(
                this::explicitDomains,
                this::typedDomains,
                this::explicitDimensions,
                this::typedDimensions
            ),
            minLength = 0,
            maxLength = 10000
        )

        validateIterableDpmElementsValueUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = (explicitDomains + typedDomains).addNotNull(metricDomain),
            valueSelector = { it: DpmElement -> it.code() },
            valueName = ExplicitDomain::domainCode
        )

        validateIterableDpmElementsValueUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = (explicitDomains + typedDomains).addNotNull(metricDomain),
            valueSelector = { it: DpmElement -> it.uri },
            valueName = ExplicitDomain::uri
        )

        validateIterableDpmElementsValueUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = explicitDimensions + typedDimensions,
            valueSelector = { it: DpmElement -> it.code() },
            valueName = ExplicitDimension::dimensionCode
        )

        validateIterableDpmElementsValueUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = explicitDimensions + typedDimensions,
            valueSelector = { it: DpmElement -> it.uri },
            valueName = ExplicitDimension::uri
        )

        validateDpmElementCrossReferences(
            validationResultBuilder = validationResultBuilder,
            targetElements = explicitDomains,
            targetCodeProperty = ExplicitDomain::domainCode,
            referringElements = explicitDimensions,
            referringCodeProperty = ExplicitDimension::referencedDomainCode
        )

        validateDpmElementCrossReferences(
            validationResultBuilder = validationResultBuilder,
            targetElements = typedDomains,
            targetCodeProperty = TypedDomain::domainCode,
            referringElements = typedDimensions,
            referringCodeProperty = TypedDimension::referencedDomainCode
        )

        validateDpmElementCrossReferences(
            validationResultBuilder = validationResultBuilder,
            targetElements = explicitDomains,
            targetCodeProperty = ExplicitDomain::domainCode,
            referringElements = metricDomain?.metrics ?: emptyList(),
            referringCodeProperty = Metric::referencedDomainCode
        )

        metricDomain?.metrics?.forEach loop@{ metric ->

            metric.referencedDomainCode ?: return@loop
            metric.referencedHierarchyCode ?: return@loop

            val domain = explicitDomains.find { it.domainCode == metric.referencedDomainCode }

            if (domain != null) {

                validateDpmElementCrossReferences(
                    validationResultBuilder = validationResultBuilder,
                    targetElements = domain.hierarchies,
                    targetCodeProperty = Hierarchy::hierarchyCode,
                    referringElements = listOf(metric),
                    referringCodeProperty = Metric::referencedHierarchyCode
                )
            }
        }
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "DPM Dictionary",
            subjectIdentifiers = listOf(owner.prefix)
        )
    }

    private fun <T> List<T>.addNotNull(element: T?): List<T> =
        if (element != null) {
            this + element
        } else {
            this
        }
}
