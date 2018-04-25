package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class ExplicitDomain(
    override val concept: Concept,
    override val domainCode: String,
    val members: List<ExplicitDomainMember>,
    val defaultMemberIndex: Int
) : Domain {

    init {
        domainValidation()

        validateProperty(
            instance = this,
            property = "members",
            minLength = 1,
            maxLength = 10000
        )

        require(members.indices.contains(defaultMemberIndex)) {
            "defaultMemberIndex must be valid index within members list"
        }
    }
}

