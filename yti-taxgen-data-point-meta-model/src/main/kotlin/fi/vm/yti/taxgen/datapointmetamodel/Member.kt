package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class Member(
    val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {
        validateProperty(
            instance = this,
            property = "memberCode",
            minLength = 2,
            maxLength = 50
        )
    }
}
