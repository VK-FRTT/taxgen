package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmCodeContent
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class Member(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Member::memberCode,
            minLength = 1,
            maxLength = 50
        )

        validateDpmCodeContent(
            validationResults = validationResults,
            instance = this,
            property = Member::memberCode
        )
    }
}
