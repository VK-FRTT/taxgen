package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmCodeContent
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength

data class Member(
    override val uri: String,
    override val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean
) : DpmElement {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateDpmElement(validationResultBuilder)

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::memberCode,
            minLength = 1,
            maxLength = 50
        )

        validateDpmCodeContent(
            validationResultBuilder = validationResultBuilder,
            property = this::memberCode
        )
    }
}
