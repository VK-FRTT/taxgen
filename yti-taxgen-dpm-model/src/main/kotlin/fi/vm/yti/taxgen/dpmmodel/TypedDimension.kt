package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateLengths

data class TypedDimension(
    override val uri: String,
    override val concept: Concept,
    val dimensionCode: String,
    val referencedDomainCode: String
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(TypedDimension::dimensionCode, TypedDimension::referencedDomainCode),
            minLength = 2,
            maxLength = 50
        )
    }

    override fun code(): String = dimensionCode
}
