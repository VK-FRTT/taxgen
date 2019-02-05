package fi.vm.yti.taxgen.sqliteprovider.writers

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.MemberItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbHierarchies {

    fun writeHierarchyAndAndNodes(
        dictionaryItem: DpmDictionaryItem,
        hierarchy: Hierarchy,
        domainId: EntityID<Int>,
        memberItems: Map<String, MemberItem>
    ): EntityID<Int> {

        return transaction {
            val hierarchyConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                hierarchy.concept,
                ConceptType.HIERARCHY
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
                    dictionaryItem,
                    currentNode.concept,
                    ConceptType.HIERARCHY_NODE
                )

                insertHierarchyNode(
                    memberItems,
                    hierarchyNodeConceptId,
                    hierarchyId,
                    parentNode,
                    currentNode,
                    currentLevel,
                    order
                )
            }

            hierarchyId
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
        memberItems: Map<String, MemberItem>,
        hierarchyNodeConceptId: EntityID<Int>,
        hierarchyId: EntityID<Int>,
        parentNode: HierarchyNode?,
        currentNode: HierarchyNode,
        currentLevel: Int,
        order: Int
    ) {
        val memberItem = memberItems[currentNode.referencedMemberUri] ?: thisShouldNeverHappen("ReferencedMemberUri refers to unknown Member")

        val parentMemberItem = parentNode?.let {
            memberItems[it.referencedMemberUri] ?: thisShouldNeverHappen("Parent ReferencedMemberUri refers to unknown Member")
        }

        val defaultLabel = currentNode.concept.label.defaultTranslation() ?: memberItem.defaultLabelText

        HierarchyNodeTable.insert {
            it[hierarchyIdCol] = hierarchyId
            it[memberIdCol] = memberItem.memberId
            it[isAbstractCol] = currentNode.abstract
            it[comparisonOperatorCol] = currentNode.comparisonOperator
            it[unaryOperatorCol] = currentNode.unaryOperator
            it[orderCol] = order
            it[levelCol] = currentLevel
            it[parentMemberID] = parentMemberItem?.memberId?.value
            it[hierarchyNodeLabel] = defaultLabel
            it[conceptIdCol] = hierarchyNodeConceptId
            it[pathCol] = null
        }
    }
}
