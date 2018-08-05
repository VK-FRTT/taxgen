package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors

data class DpmDictionary(
    val owner: Owner,
    val explicitDomains: List<ExplicitDomain>
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {
    }
}
