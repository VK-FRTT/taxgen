package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterablePropertyValuesUnique
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class DpmDictionary(
    val owner: Owner,
    val explicitDomains: List<ExplicitDomain>
) : Validatable {

    override fun validate(validationResults: ValidationResults) {

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = DpmDictionary::explicitDomains,
            minLength = 1,
            maxLength = 10000
        )

        validateIterablePropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = DpmDictionary::explicitDomains,
            valueProperty = ExplicitDomain::id
        )

        validateIterablePropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = DpmDictionary::explicitDomains,
            valueProperty = ExplicitDomain::domainCode
        )

        // TODO: Validate that domain codes do not overlap (typed + explicit)
        // TODO: Validate that dimension codes do not overlap (typed + explicit)
    }
}
