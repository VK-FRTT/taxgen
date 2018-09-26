package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterableKeysUnique
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class DpmDictionary(
    val owner: Owner,
    val explicitDomains: List<ExplicitDomain>
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = DpmDictionary::explicitDomains,
            minLength = 1,
            maxLength = 10000
        )

        validateIterableKeysUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = DpmDictionary::explicitDomains,
            keyProperty = ExplicitDomain::domainCode
        )
    }
}
