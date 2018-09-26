package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class Member(
    override val id: String,
    override val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean
) : DpmElement {

    override fun validate(validationErrors: ValidationErrors) {

        super.validate(validationErrors)

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Member::memberCode,
            minLength = 2,
            maxLength = 50
        )
    }
}
