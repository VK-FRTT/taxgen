package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
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
        domainId: EntityID<Int>,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions
    ) {

        hierarchies.forEach { hierarchy ->
            DbHierarchies.writeHierarchyAndAndNodes(
                hierarchy,
                domainId,
                ownerId,
                languageIds,
                processingOptions
            )
        }
    }

    private fun writeHierarchyAndAndNodes(
        hierarchy: Hierarchy,
        domainId: EntityID<Int>,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions
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
                domainId,
                processingOptions
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
                    processingOptions
                )
            }
        }
    }

    private fun insertHierarchy(
        hierarchy: Hierarchy,
        hierarchyConceptId: EntityID<Int>,
        domainId: EntityID<Int>,
        processingOptions: ProcessingOptions
    ): EntityID<Int> {

        val hierarchyId = HierarchyTable.insertAndGetId {
            it[hierarchyCodeCol] = hierarchy.hierarchyCode
            it[hierarchyLabelCol] = hierarchy.concept.label.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
            it[hierarchyDescriptionCol] = hierarchy.concept.description.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
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
        processingOptions: ProcessingOptions
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

        HierarchyNodeTable.insert {
            it[hierarchyIdCol] = hierarchyId
            it[memberIdCol] = memberId
            it[isAbstractCol] = currentNode.abstract
            it[comparisonOperatorCol] = currentNode.comparisonOperator
            it[unaryOperatorCol] = currentNode.unaryOperator
            it[orderCol] = order
            it[levelCol] = currentLevel
            it[parentMemberID] = parentMemberId?.value
            it[hierarchyNodeLabel] = currentNode.concept.label.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
            it[conceptIdCol] = hierarchyNodeConceptId
            it[pathCol] = null
        }
    }
}
