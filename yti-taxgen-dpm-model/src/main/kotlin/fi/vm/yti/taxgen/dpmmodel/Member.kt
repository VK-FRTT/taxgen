package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateDpmCodeContent
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateLength

data class Member(
    override val uri: String,
    override val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        validateDpmElement(validationResults)

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
