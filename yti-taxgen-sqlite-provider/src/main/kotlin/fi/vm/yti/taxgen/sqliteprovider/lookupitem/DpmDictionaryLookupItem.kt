package fi.vm.yti.taxgen.sqliteprovider.lookupitem

import org.jetbrains.exposed.dao.EntityID

data class DpmDictionaryLookupItem(
    val domainLookupItems: List<DomainLookupItem>,
    val dimensionLookupItems: List<DimensionLookupItem>,
    val ownerId: EntityID<Int>
)
