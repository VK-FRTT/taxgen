package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementPropertyValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLengths

data class DpmDictionary(
    val owner: Owner,
    val metrics: List<Metric>,
    val explicitDomains: List<ExplicitDomain>,
    val typedDomains: List<TypedDomain>,
    val explicitDimensions: List<ExplicitDimension>,
    val typedDimensions: List<TypedDimension>
) : Validatable {

    override fun validate(validationResults: ValidationResults) {

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(
                DpmDictionary::metrics,
                DpmDictionary::explicitDomains,
                DpmDictionary::typedDomains,
                DpmDictionary::explicitDimensions,
                DpmDictionary::typedDimensions
            ),
            minLength = 1,
            maxLength = 10000
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = DpmDictionary::metrics,
            valueProperties = listOf(Metric::id, Metric::memberCodeNumber)
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = DpmDictionary::explicitDomains,
            valueProperties = listOf(ExplicitDomain::id, ExplicitDomain::domainCode)
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = DpmDictionary::typedDomains,
            valueProperties = listOf(TypedDomain::id, TypedDomain::domainCode)
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = DpmDictionary::explicitDimensions,
            valueProperties = listOf(ExplicitDimension::id, ExplicitDimension::dimensionCode)
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = DpmDictionary::typedDimensions,
            valueProperties = listOf(TypedDimension::id, TypedDimension::dimensionCode)
        )

        // TODO: Validate that domain codes do not overlap (typed + explicit)
        // TODO: Validate that dimension codes do not overlap (typed + explicit)
        // TODO: Should dimension.domainRefs be validated as unique ?
    }
}
