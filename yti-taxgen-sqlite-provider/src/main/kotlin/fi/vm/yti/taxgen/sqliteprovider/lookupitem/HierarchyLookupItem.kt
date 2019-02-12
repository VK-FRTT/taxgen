package fi.vm.yti.taxgen.sqliteprovider.lookupitem

import org.jetbrains.exposed.dao.EntityID

data class HierarchyLookupItem(
    val hierarchyCode: String,
    val hierarchyId: EntityID<Int>
)
