package fi.vm.yti.taxgen.sqliteoutput.conceptwriter

import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptType
import fi.vm.yti.taxgen.sqliteoutput.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteoutput.tables.DomainTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MetricTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

object DbDictionaries {

    fun writeDictionaryBaseParts(
        dictionary: DpmDictionary,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions
    ) {
        dictionary.explicitDomains.forEach { explicitDomain ->

            val explicitDomainId = DbDomains.writeExplicitDomainAndMembers(
                explicitDomain,
                dictionary.owner,
                ownerId,
                languageIds,
                processingOptions
            )

            DbHierarchies.writeHierarchiesAndAndNodes(
                explicitDomain.hierarchies,
                explicitDomainId,
                ownerId,
                languageIds,
                processingOptions
            )
        }

        dictionary.typedDomains.forEach { typedDomain ->
            DbDomains.writeTypedDomain(
                typedDomain,
                dictionary.owner,
                ownerId,
                languageIds,
                processingOptions
            )
        }

        dictionary.explicitDimensions.forEach { explicitDimension ->
            DbDimensions.writeExplicitDimension(
                explicitDimension,
                dictionary.owner,
                ownerId,
                languageIds,
                processingOptions
            )
        }

        dictionary.typedDimensions.forEach { typedDimension ->
            DbDimensions.writeTypedDimension(
                typedDimension,
                dictionary.owner,
                ownerId,
                languageIds,
                processingOptions
            )
        }
    }

    fun writeDictionaryMetricsToFixedDomain(
        dpmDictionary: DpmDictionary,
        ownerId: EntityID<Int>,
        metricDomainId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions
    ) {
        dpmDictionary.metricDomain?.let {

            DbMetric.writeMetricDomainMembers(
                it,
                metricDomainId,
                dpmDictionary.owner,
                ownerId,
                languageIds,
                processingOptions
            )

            DbHierarchies.writeHierarchiesAndAndNodes(
                it.hierarchies,
                metricDomainId,
                ownerId,
                languageIds,
                processingOptions
            )
        }
    }

    fun purgeDictionaryContent() {
        transaction {

            HierarchyNodeTable.deleteAll()
            ConceptTable.deleteConceptsAndTranslationsOfType(ConceptType.HIERARCHY_NODE)

            HierarchyTable.deleteAll()
            ConceptTable.deleteConceptsAndTranslationsOfType(ConceptType.HIERARCHY)

            MetricTable.deleteAll()
            MemberTable.deleteAll()
            ConceptTable.deleteConceptsAndTranslationsOfType(ConceptType.MEMBER)

            DomainTable.deleteAll()
            ConceptTable.deleteConceptsAndTranslationsOfType(ConceptType.DOMAIN)

            DimensionTable.deleteAll()
            ConceptTable.deleteConceptsAndTranslationsOfType(ConceptType.DIMENSION)
        }
    }
}
