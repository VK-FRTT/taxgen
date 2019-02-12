package fi.vm.yti.taxgen.sqliteprovider.lookupitem

import org.jetbrains.exposed.dao.EntityID

data class DomainLookupItem(
    val domainCode: String,
    val memberLookupItems: List<MemberLookupItem>,
    val hierarchyLookupItems: List<HierarchyLookupItem>,
    val domainId: EntityID<Int>
)
