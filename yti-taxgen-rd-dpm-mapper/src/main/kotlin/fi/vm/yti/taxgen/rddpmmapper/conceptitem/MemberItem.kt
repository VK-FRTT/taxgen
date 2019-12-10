package fi.vm.yti.taxgen.rddpmmapper.conceptitem

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Member

data class MemberItem(
    override val uri: String,
    val concept: Concept,
    val memberCode: String,
    val defaultMember: Boolean,
    val order: Int
) : UriIdentifiedItem {

    fun toMember(): Member {
        return Member(
            uri = uri,
            concept = concept,
            memberCode = memberCode,
            defaultMember = defaultMember
        )
    }
}
