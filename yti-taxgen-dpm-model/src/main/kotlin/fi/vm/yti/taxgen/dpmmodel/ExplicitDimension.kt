package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateLengths

data class ExplicitDimension(
    override val uri: String,
    override val concept: Concept,
    val dimensionCode: String,
    val referencedDomainCode: String
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        validateDpmElement(validationResults)

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(ExplicitDimension::dimensionCode, ExplicitDimension::referencedDomainCode),
            minLength = 2,
            maxLength = 50
        )
    }

    override fun code(): String = dimensionCode
}
