package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class ExplicitDomain(
    val concept: Concept,
    val domainCode: String,
    val members: List<Member>
) {

    init {
        validateProperty(
            instance = this,
            property = "domainCode",
            minLength = 2,
            maxLength = 50
        )

        validateProperty(
            instance = this,
            property = "members",
            minLength = 1,
            maxLength = 10000
        )

        //TODO members validation
        // - Each Code must be unique
        // - Only SINGLE default member exists
    }
}
