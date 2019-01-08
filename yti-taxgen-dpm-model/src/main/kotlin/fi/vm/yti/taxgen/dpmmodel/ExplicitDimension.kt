package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateLengths

data class ExplicitDimension(
    override val uri: String,
    override val concept: Concept,
    val dimensionCode: String,
    val referencedDomainCode: String //TODO - validate ref points to ExplicitDomain?
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(ExplicitDimension::dimensionCode, ExplicitDimension::referencedDomainCode),
            minLength = 2,
            maxLength = 50
        )
    }
}
