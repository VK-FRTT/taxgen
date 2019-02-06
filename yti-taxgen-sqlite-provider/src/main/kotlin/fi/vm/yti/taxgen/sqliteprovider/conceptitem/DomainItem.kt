package fi.vm.yti.taxgen.sqliteprovider.conceptitem

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import org.jetbrains.exposed.dao.EntityID

data class DomainItem(
    val domainCode: String,
    val domainId: EntityID<Int>,
    val hierarchyItems: List<HierarchyItem>
) {
    fun optionalHierarchyItemForCode(hierarchyCode: String?): HierarchyItem? {
        if (hierarchyCode == null) {
            return null
        }

        return hierarchyItems.find { it.hierarchyCode == hierarchyCode }
            ?: thisShouldNeverHappen("No hierarchy for given code: $hierarchyCode")
    }
}
