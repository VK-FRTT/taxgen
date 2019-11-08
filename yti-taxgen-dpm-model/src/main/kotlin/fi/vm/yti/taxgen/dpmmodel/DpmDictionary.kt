package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateCustom
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateElementValueUnique
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateLengths

data class DpmDictionary(
    val owner: Owner,
    val explicitDomains: List<ExplicitDomain>,
    val typedDomains: List<TypedDomain>,
    val explicitDimensions: List<ExplicitDimension>,
    val typedDimensions: List<TypedDimension>,
    val metricDomain: MetricDomain?
) : Validatable {

    private fun <T> List<T>.addNotNull(element: T?): List<T> =
        if (element != null) {
            this + element
        } else {
            this
        }

    override fun validate(validationResults: ValidationResults) {

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(
                DpmDictionary::explicitDomains,
                DpmDictionary::typedDomains,
                DpmDictionary::explicitDimensions,
                DpmDictionary::typedDimensions
            ),
            minLength = 0,
            maxLength = 10000
        )

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "domains",
            iterable = (explicitDomains + typedDomains).addNotNull(metricDomain),
            valueSelector = { it: DpmElement -> it.code() },
            valueDescription = "domainCode"
        )

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "domains",
            iterable = (explicitDomains + typedDomains).addNotNull(metricDomain),
            valueSelector = { it: DpmElement -> it.uri },
            valueDescription = "uri"
        )

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "dimensions",
            iterable = explicitDimensions + typedDimensions,
            valueSelector = { it: DpmElement -> it.code() },
            valueDescription = "dimensionCode"
        )

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "dimensions",
            iterable = explicitDimensions + typedDimensions,
            valueSelector = { it: DpmElement -> it.uri },
            valueDescription = "uri"
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "explicitDimensions",
            validate = { messages ->
                explicitDimensions.forEach { explicitDimension ->
                    val referencedDomain =
                        explicitDomains.find { it.domainCode == explicitDimension.referencedDomainCode }

                    if (referencedDomain == null) {
                        messages.add("ExplicitDimension ${explicitDimension.uri} refers non existing ExplicitDomain '${explicitDimension.referencedDomainCode}'")
                    }
                }
            }
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "typedDimensions",
            validate = { messages ->
                typedDimensions.forEach { typedDimension ->
                    val referencedDomain =
                        typedDomains.find { it.domainCode == typedDimension.referencedDomainCode }

                    if (referencedDomain == null) {
                        messages.add("TypedDimension ${typedDimension.uri} refers non existing TypedDomain '${typedDimension.referencedDomainCode}'")
                    }
                }
            }
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "metrics",
            validate = { messages ->
                metricDomain?.metrics?.forEach { metric ->

                    if (metric.referencedDomainCode != null) {
                        val referencedDomain =
                            explicitDomains.find { it.domainCode == metric.referencedDomainCode }

                        if (referencedDomain == null) {
                            messages.add("Metric ${metric.uri} refers non existing ExplicitDomain '${metric.referencedDomainCode}'")
                        } else {

                            if (metric.referencedHierarchyCode != null) {
                                val referencedHierarchy =
                                    referencedDomain.hierarchies.find { it.hierarchyCode == metric.referencedHierarchyCode }

                                if (referencedHierarchy == null) {
                                    messages.add("Metric ${metric.uri} refers non existing Hierarchy '${metric.referencedHierarchyCode}' (not part of referenced ExplicitDomain ${referencedDomain.uri})")
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
