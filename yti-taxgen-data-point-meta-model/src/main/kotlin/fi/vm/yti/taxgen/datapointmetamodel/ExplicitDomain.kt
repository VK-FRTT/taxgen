package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class ExplicitDomain(
    override val concept: Concept,
    override val domainCode: String,
    val members: List<ExplicitDomainMember>
) : Domain {

    init {
        domainValidation()

        validateProperty(
            instance = this,
            property = "members",
            minLength = 1,
            maxLength = 10000
        )

        //TODO members validation
        // - Each Code must be unique
        // - Single default member must exist
    }
}
