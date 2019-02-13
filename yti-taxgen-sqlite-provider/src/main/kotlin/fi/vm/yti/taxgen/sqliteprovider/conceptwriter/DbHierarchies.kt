package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.HierarchyLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.MemberLookupItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbHierarchies {

    fun writeHierarchiesAndAndNodes(
        hierarchies: List<Hierarchy>,
        domainId: EntityID<Int>,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        memberLookupItems: List<MemberLookupItem>
    ): List<HierarchyLookupItem> {

        return hierarchies.map { hierarchy ->
            DbHierarchies.writeHierarchyAndAndNodes(
                hierarchy,
                domainId,
                ownerId,
                languageIds,
                memberLookupItems
            )
        }
    }

    private fun writeHierarchyAndAndNodes(
        hierarchy: Hierarchy,
        domainId: EntityID<Int>,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        memberLookupItems: List<MemberLookupItem>
    ): HierarchyLookupItem {

        return transaction {
            val hierarchyConceptId = DbConcepts.writeConceptAndTranslations(
                hierarchy.concept,
                ConceptType.HIERARCHY,
                ownerId,
                languageIds
            )

            val hierarchyId = insertHierarchy(
                hierarchy,
                hierarchyConceptId,
                domainId
            )

            var order = 0
            hierarchy.traverseNodesInPreOrder { parentNode, currentNode, currentLevel ->
                order++

                val hierarchyNodeConceptId = DbConcepts.writeConceptAndTranslations(
                    currentNode.concept,
                    ConceptType.HIERARCHY_NODE,
                    ownerId,
                    languageIds
                )

                insertHierarchyNode(
                    hierarchyNodeConceptId,
                    hierarchyId,
                    parentNode,
                    currentNode,
                    currentLevel,
                    order,
                    memberLookupItems
                )
            }

            HierarchyLookupItem(
                hierarchyCode = hierarchy.hierarchyCode,
                hierarchyId = hierarchyId
            )
        }
    }

    private fun insertHierarchy(
        hierarchy: Hierarchy,
        hierarchyConceptId: EntityID<Int>,
        domainId: EntityID<Int>
    ): EntityID<Int> {

        val hierarchyId = HierarchyTable.insertAndGetId {
            it[hierarchyCodeCol] = hierarchy.hierarchyCode
            it[hierarchyLabelCol] = hierarchy.concept.label.defaultTranslation()
            it[hierarchyDescriptionCol] = hierarchy.concept.description.defaultTranslation()
            it[domainIdCol] = domainId
            it[conceptIdCol] = hierarchyConceptId
        }

        return hierarchyId
    }

    private fun insertHierarchyNode(
        hierarchyNodeConceptId: EntityID<Int>,
        hierarchyId: EntityID<Int>,
        parentNode: HierarchyNode?,
        currentNode: HierarchyNode,
        currentLevel: Int,
        order: Int,
        memberLookupItems: List<MemberLookupItem>
    ) {
        val memberLookupItem =
            memberLookupItems.find { it.memberUri == currentNode.referencedMemberUri }
                ?: thisShouldNeverHappen("No Member matching CurrentNode.ReferencedMemberUri: ${currentNode.referencedMemberUri}")

        val parentMemberLookupItem =
            if (parentNode == null) {
                null
            } else {
                memberLookupItems.find { it.memberUri == parentNode.referencedMemberUri }
                    ?: thisShouldNeverHappen("No Member matching ParentNode.ReferencedMemberUri: ${parentNode.referencedMemberUri}")
            }

        val defaultLabel = currentNode.concept.label.defaultTranslation() ?: memberLookupItem.defaultLabelText

        HierarchyNodeTable.insert {
            it[hierarchyIdCol] = hierarchyId
            it[memberIdCol] = memberLookupItem.memberId
            it[isAbstractCol] = currentNode.abstract
            it[comparisonOperatorCol] = currentNode.comparisonOperator
            it[unaryOperatorCol] = currentNode.unaryOperator
            it[orderCol] = order
            it[levelCol] = currentLevel
            it[parentMemberID] = parentMemberLookupItem?.memberId?.value
            it[hierarchyNodeLabel] = defaultLabel
            it[conceptIdCol] = hierarchyNodeConceptId
            it[pathCol] = null
        }
    }
}
