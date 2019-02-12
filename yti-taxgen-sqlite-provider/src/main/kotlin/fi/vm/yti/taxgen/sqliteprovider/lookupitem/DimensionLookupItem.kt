package fi.vm.yti.taxgen.sqliteprovider.lookupitem

import org.jetbrains.exposed.dao.EntityID

data class DimensionLookupItem(
    val dimensionXbrlCode: String,
    val dimensionId: EntityID<Int>
)
