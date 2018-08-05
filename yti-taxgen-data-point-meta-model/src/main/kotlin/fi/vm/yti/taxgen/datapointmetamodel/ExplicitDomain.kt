package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors

data class ExplicitDomain(
    val concept: Concept,
    val domainCode: String,
    val members: List<Member>
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {
        /*
        //TODO
        messages += concept.validateData()

        messages += validateSize(
            instance = this,
            property = ExplicitDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )

        messages += validateSize(
            instance = this,
            property = ExplicitDomain::members,
            minLength = 1,
            maxLength = 10000
        )


        members.groupingBy { it.memberCode }.eachCount().filter { it.value > 1 }.keys.let { dublicateMemberCodes ->
            if (dublicateMemberCodes.any()) {
                messages.add("Multiple members with same member codes: ${dublicateMemberCodes}")
            }
        }

        members.filter{it.defaultMember}.size.let {defaultMembersAmount->
            if(defaultMembersAmount == 0){
                messages.add("Multiple members with same member codes: ${dublicateMemberCodes}")
            }

            if(defaultMembersAmount > 1) {
                messages.add("Multiple members with same member codes: ${dublicateMemberCodes}")
            }
        }
        */
    }
}
