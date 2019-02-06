package fi.vm.yti.taxgen.sqliteprovider.conceptitem

import org.jetbrains.exposed.dao.EntityID

data class HierarchyItem(
    val hierarchyCode: String,
    val hierarchyId: EntityID<Int>
)
