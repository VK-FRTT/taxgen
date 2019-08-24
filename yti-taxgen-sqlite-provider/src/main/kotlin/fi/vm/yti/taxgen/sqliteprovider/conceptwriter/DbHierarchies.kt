package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbHierarchies {

    fun writeHierarchiesAndAndNodes(
        hierarchies: List<Hierarchy>,
        referencedElementConceptsByCode: Map<String, Concept>,
        domainId: EntityID<Int>,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>
    ) {

        hierarchies.forEach { hierarchy ->
            DbHierarchies.writeHierarchyAndAndNodes(
                hierarchy,
                referencedElementConceptsByCode,
                domainId,
                ownerId,
                languageIds
            )
        }
    }

    private fun writeHierarchyAndAndNodes(
        hierarchy: Hierarchy,
        referencedElementConceptsByCode: Map<String, Concept>,
        domainId: EntityID<Int>,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>
    ) {

        transaction {
            val hierarchyConceptId = DbConcepts.writeConceptAndTranslations(
                hierarchy,
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
                    currentNode,
                    ownerId,
                    languageIds
                )

                insertHierarchyNode(
                    hierarchyNodeConceptId,
                    hierarchyId,
                    parentNode,
                    currentNode,
                    domainId,
                    currentLevel,
                    order,
                    referencedElementConceptsByCode[currentNode.referencedElementCode]
                        ?: thisShouldNeverHappen("No Concept found for ReferencedElementCode: ${currentNode.referencedElementCode}")
                )
            }
        }
    }

    private fun insertHierarchy(
        hierarchy: Hierarchy,
        hierarchyConceptId: EntityID<Int>,
        domainId: EntityID<Int>
    ): EntityID<Int> {

        val hierarchyId = HierarchyTable.insertAndGetId {
            it[hierarchyCodeCol] = hierarchy.hierarchyCode
            it[hierarchyLabelCol] = hierarchy.concept.label.defaultTranslationOrNull()
            it[hierarchyDescriptionCol] = hierarchy.concept.description.defaultTranslationOrNull()
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
        memberDomainId: EntityID<Int>,
        currentLevel: Int,
        order: Int,
        referencedElementConcept: Concept
    ) {
        val memberRow = MemberTable.rowWhereDomainIdAndMemberCode(memberDomainId, currentNode.referencedElementCode)
            ?: thisShouldNeverHappen("No Member matching CurrentNode.referencedElementCode: ${currentNode.referencedElementCode}")

        val memberId = memberRow[MemberTable.id]

        val parentMemberRow =
            if (parentNode == null) {
                null
            } else {
                MemberTable.rowWhereDomainIdAndMemberCode(memberDomainId, parentNode.referencedElementCode)
                    ?: thisShouldNeverHappen("No Member matching ParentNode.referencedElementCode: ${parentNode.referencedElementCode}")
            }

        val parentMemberId = parentMemberRow?.get(MemberTable.id)

        val nodeDefaultLabel = currentNode.concept.label.defaultTranslationOrNull()
            ?: referencedElementConcept.label.defaultTranslationOrNull()

        HierarchyNodeTable.insert {
            it[hierarchyIdCol] = hierarchyId
            it[memberIdCol] = memberId
            it[isAbstractCol] = currentNode.abstract
            it[comparisonOperatorCol] = currentNode.comparisonOperator
            it[unaryOperatorCol] = currentNode.unaryOperator
            it[orderCol] = order
            it[levelCol] = currentLevel
            it[parentMemberID] = parentMemberId?.value
            it[hierarchyNodeLabel] = nodeDefaultLabel
            it[conceptIdCol] = hierarchyNodeConceptId
            it[pathCol] = null
        }
    }
}
