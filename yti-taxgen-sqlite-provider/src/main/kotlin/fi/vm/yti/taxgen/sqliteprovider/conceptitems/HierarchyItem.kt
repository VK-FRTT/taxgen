package fi.vm.yti.taxgen.sqliteprovider.conceptitems

import org.jetbrains.exposed.dao.EntityID

data class HierarchyItem(
    val hierarchyCode: String,
    val hierarchyId: EntityID<Int>
)
