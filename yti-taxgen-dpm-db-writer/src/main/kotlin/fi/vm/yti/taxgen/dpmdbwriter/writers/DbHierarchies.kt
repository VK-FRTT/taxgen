package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.datapointmetamodel.Hierarchy
import fi.vm.yti.taxgen.datapointmetamodel.HierarchyNode
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.dpmdbwriter.DpmDictionaryWriteContext
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptType
import fi.vm.yti.taxgen.dpmdbwriter.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.HierarchyTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.IdentityHashMap
import java.util.LinkedList

object DbHierarchies {

    fun writeHierarchyAndAndNodes(
        writeContext: DpmDictionaryWriteContext,
        hierarchy: Hierarchy,
        domainId: EntityID<Int>,
        memberIds: IdentityHashMap<Member, EntityID<Int>>
    ) {

        transaction {
            val hierarchyConceptId = DbConcepts.writeConceptAndTranslations(
                writeContext,
                hierarchy.concept,
                ConceptType.HIERARCHY
            )

            val hierarchyId = insertHierarchy(
                hierarchy,
                hierarchyConceptId,
                domainId
            )

            val hierarchyTreeContext = HierarchyTreeContext(hierarchyId, memberIds)

            hierarchy.rootNodes.forEachIndexed { index, hierarchyNode ->
                hierarchyTreeContext.withNode(hierarchyNode) {
                    writeHierarchyNodeAndChilds(
                        writeContext,
                        hierarchyTreeContext,
                        index
                    )
                }
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
            it[hierarchyLabelCol] = hierarchy.concept.label.defaultText()
            it[hierarchyDescriptionCol] = hierarchy.concept.description.defaultText()
            it[domainIdCol] = domainId
            it[conceptIdCol] = hierarchyConceptId
        }

        return hierarchyId
    }

    private fun writeHierarchyNodeAndChilds(
        writeContext: DpmDictionaryWriteContext,
        hierarchyTreeContext: HierarchyTreeContext,
        hierarchyNodeIndex: Int
    ) {
        val hierarchyNodeConceptId = DbConcepts.writeConceptAndTranslations(
            writeContext,
            hierarchyTreeContext.node().concept,
            ConceptType.HIERARCHY_NODE
        )

        insertHierarchyNode(
            hierarchyTreeContext = hierarchyTreeContext,
            hierarchyNodeConceptId = hierarchyNodeConceptId,
            hierarchyNodeIndex = hierarchyNodeIndex
        )

        hierarchyTreeContext.node().childNodes?.forEachIndexed { childIndex, childNode ->
            hierarchyTreeContext.withNode(childNode) {
                writeHierarchyNodeAndChilds(
                    writeContext = writeContext,
                    hierarchyTreeContext = hierarchyTreeContext,
                    hierarchyNodeIndex = childIndex
                )
            }
        }
    }

    private fun insertHierarchyNode(
        hierarchyTreeContext: HierarchyTreeContext,
        hierarchyNodeConceptId: EntityID<Int>,
        hierarchyNodeIndex: Int
    ) {
        val node = hierarchyTreeContext.node()

        HierarchyNodeTable.insert {
            it[hierarchyIdCol] = hierarchyTreeContext.hierarchyId()
            it[memberIdCol] = hierarchyTreeContext.memberId()
            it[isAbstractCol] = node.abstract
            it[comparisonOperatorCol] = node.comparisonOperator
            it[unaryOperatorCol] = node.unaryOperator
            it[orderCol] = hierarchyNodeIndex + 1
            it[levelCol] = hierarchyTreeContext.level()
            it[parentMemberID] = hierarchyTreeContext.parentMemberId()?.value
            it[hierarchyNodeLabel] = node.concept.label.defaultText()
            it[conceptIdCol] = hierarchyNodeConceptId
            it[pathCol] = hierarchyTreeContext.path()
        }
    }

    private class HierarchyTreeContext(
        private val hierarchyId: EntityID<Int>,
        private val memberIds: IdentityHashMap<Member, EntityID<Int>>
    ) {
        private val nodeStack = LinkedList<HierarchyNode>()

        fun hierarchyId(): EntityID<Int> {
            return hierarchyId
        }

        fun node(): HierarchyNode {
            return nodeStack.peek() ?: thisShouldNeverHappen("Node stack empty")
        }

        fun memberId(): EntityID<Int> {
            return memberIds[node().member] ?: thisShouldNeverHappen("No ID for Member")
        }

        fun parentMemberId(): EntityID<Int>? {
            if (nodeStack.size < 2) return null

            val parentNode = nodeStack[1]

            return memberIds[parentNode.member] ?: thisShouldNeverHappen("No ID for parent Member")
        }

        fun level(): Int {
            return nodeStack.size
        }

        fun path(): String? {
            return null
        }

        fun withNode(node: HierarchyNode, block: () -> Unit) {
            nodeStack.push(node)
            block()
            nodeStack.pop()
        }
    }
}
