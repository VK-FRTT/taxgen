package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropsLengths

data class ExplicitDimension(
    override val uri: String,
    override val concept: Concept,
    val dimensionCode: String,
    val referencedDomainCode: String
) : DpmElement {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {
        validateDpmElement(validationResultBuilder)

        validatePropsLengths(
            validationResultBuilder = validationResultBuilder,
            properties = listOf(this::dimensionCode, this::referencedDomainCode),
            minLength = 2,
            maxLength = 50
        )
    }

    override fun code(): String = dimensionCode
}
