package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MetricTable
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
        dpmDictionary.metricDomains.forEach { metricDomain ->

            DbMetric.writeMetricDomainMembers(
                metricDomain,
                metricDomainId,
                dpmDictionary.owner,
                ownerId,
                languageIds,
                processingOptions
            )

            DbHierarchies.writeHierarchiesAndAndNodes(
                metricDomain.hierarchies,

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
            DbConcepts.deleteAllConceptsAndTranslations(ConceptType.HIERARCHY_NODE)

            HierarchyTable.deleteAll()
            DbConcepts.deleteAllConceptsAndTranslations(ConceptType.HIERARCHY)

            MetricTable.deleteAll()
            MemberTable.deleteAll()
            DbConcepts.deleteAllConceptsAndTranslations(ConceptType.MEMBER)

            DomainTable.deleteAll()
            DbConcepts.deleteAllConceptsAndTranslations(ConceptType.DOMAIN)

            DimensionTable.deleteAll()
            DbConcepts.deleteAllConceptsAndTranslations(ConceptType.DIMENSION)
        }
    }
}
