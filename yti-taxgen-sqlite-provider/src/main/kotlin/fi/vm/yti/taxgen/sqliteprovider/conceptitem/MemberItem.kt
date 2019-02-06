package fi.vm.yti.taxgen.sqliteprovider.conceptitem

import org.jetbrains.exposed.dao.EntityID

data class MemberItem(
    val memberId: EntityID<Int>,
    val defaultLabelText: String?
)
