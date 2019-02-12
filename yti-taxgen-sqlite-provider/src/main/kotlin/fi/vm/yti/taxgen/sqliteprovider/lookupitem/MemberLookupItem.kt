package fi.vm.yti.taxgen.sqliteprovider.lookupitem

import org.jetbrains.exposed.dao.EntityID

data class MemberLookupItem(
    val memberUri: String,
    val memberXbrlCode: String,
    val defaultLabelText: String?,
    val memberId: EntityID<Int>
)
