package fi.vm.yti.taxgen.sqliteprovider.lookupitem

import org.jetbrains.exposed.dao.EntityID

data class FixedEntitiesLookupItem(
    val metricDomainCode: String,
    val metricDomainId: EntityID<Int>,
    val openMemberId: EntityID<Int>
)
