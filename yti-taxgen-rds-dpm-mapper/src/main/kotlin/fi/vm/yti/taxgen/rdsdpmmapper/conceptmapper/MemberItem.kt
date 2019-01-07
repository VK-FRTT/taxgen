package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.Member

data class MemberItem(
    override val uri: String,
    override val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean,
    val order: Int
) : DpmElement {

    fun toMember(): Member {
        return Member(
            uri = uri,
            concept = concept,
            memberCode = memberCode,
            defaultMember = defaultMember
        )
    }
}
