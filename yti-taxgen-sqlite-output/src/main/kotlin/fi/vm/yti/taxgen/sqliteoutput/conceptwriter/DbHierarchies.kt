package fi.vm.yti.taxgen.sqliteoutput.conceptwriter

import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import org.jetbrains.exposed.dao.EntityID
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

            val hierarchyId = HierarchyTable.insertHierarchy(
                hierarchy,
                hierarchyConceptId,
                domainId,
                processingOptions.sqliteDbDpmElementInherentTextLanguage
            )

            var order = 0
            hierarchy.traverseNodesInPreOrder { parentNode, currentNode, currentLevel ->
                order++

                val hierarchyNodeConceptId = DbConcepts.writeConceptAndTranslations(
                    currentNode,
                    ownerId,
                    languageIds
                )

                HierarchyNodeTable.insertHierarchyNode(
                    hierarchyNodeConceptId,
                    hierarchyId,
                    parentNode,
                    currentNode,
                    domainId,
                    currentLevel,
                    order,
                    processingOptions.sqliteDbDpmElementInherentTextLanguage
                )
            }
        }
    }
}
