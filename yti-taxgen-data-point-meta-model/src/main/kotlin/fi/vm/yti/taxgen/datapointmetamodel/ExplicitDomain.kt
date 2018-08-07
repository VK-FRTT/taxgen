package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.customValidate
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterableElementsUnique
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class ExplicitDomain(
    val concept: Concept,
    val domainCode: String,
    val members: List<Member>
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {

        concept.validate(validationErrors)

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = ExplicitDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = ExplicitDomain::members,
            minLength = 1,
            maxLength = 10000
        )

        validateIterableElementsUnique(
            validationErrors = validationErrors,
            instance = this,
            property = ExplicitDomain::members,
            keySelector = { it.memberCode }
        )

        customValidate(
            validationErrors = validationErrors,
            instance = this,
            property = ExplicitDomain::members,
            failIf = {
                val count = members.count { it.defaultMember }
                it["count"] = count
                count != 1
            },
            failMsg = { "has ${it["count"]} default members (should have 1)" }
        )
    }
}
