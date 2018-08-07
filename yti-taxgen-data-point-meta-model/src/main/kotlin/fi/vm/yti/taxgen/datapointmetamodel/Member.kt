package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class Member(
    val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {

        concept.validate(validationErrors)

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Member::memberCode,
            minLength = 2,
            maxLength = 50
        )
    }
}
