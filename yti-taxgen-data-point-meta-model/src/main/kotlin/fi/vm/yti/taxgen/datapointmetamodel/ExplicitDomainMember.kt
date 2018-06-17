package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class ExplicitDomainMember(
    val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean
) {

    init {
        validateProperty(
            instance = this,
            property = "memberCode",
            minLength = 2,
            maxLength = 10
        )
    }
}
