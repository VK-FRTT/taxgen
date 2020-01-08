package fi.vm.yti.taxgen.sqliteoutput.conceptwriter

import org.jetbrains.exposed.dao.id.EntityID

data class FixedEntitiesIds(
    val metricDomainOwnerId: EntityID<Int>,
    val metricDomainId: EntityID<Int>,
    val metricDimensionId: EntityID<Int>,
    val openMemberId: EntityID<Int>
)
