package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DomainLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DpmDictionaryLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.HierarchyLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.MemberLookupItem
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
        languageIds: Map<Language, EntityID<Int>>
    ): DpmDictionaryLookupItem {
        val explicitDomainLookupItem = dictionary.explicitDomains.map { explicitDomain ->

            val (explicitDomainId, memberLookupItems) = DbDomains.writeExplicitDomainAndMembers(
                explicitDomain,
                dictionary.owner,
                ownerId,
                languageIds
            )

            val hierarchyLookupItems = DbHierarchies.writeHierarchiesAndAndNodes(
                explicitDomain.hierarchies,
                explicitDomainId,
                ownerId,
                languageIds,
                memberLookupItems
            )

            DomainLookupItem(
                domainCode = explicitDomain.domainCode,
                memberLookupItems = memberLookupItems,
                hierarchyLookupItems = hierarchyLookupItems,
                domainId = explicitDomainId
            )
        }

        val typedDomainLookupItems = dictionary.typedDomains.map { typedDomain ->
            val typedDomainId = DbDomains.writeTypedDomain(
                typedDomain,
                dictionary.owner,
                ownerId,
                languageIds
            )

            DomainLookupItem(
                domainCode = typedDomain.domainCode,
                memberLookupItems = emptyList(),
                hierarchyLookupItems = emptyList(),
                domainId = typedDomainId
            )
        }

        val domainLookupItems = explicitDomainLookupItem + typedDomainLookupItems

        val explicitDimensionLookupItem = dictionary.explicitDimensions.map { explicitDimension ->
            DbDimensions.writeExplicitDimension(
                explicitDimension,
                dictionary.owner,
                ownerId,
                languageIds,
                domainLookupItems
            )
        }

        val typedDimensionLookupItem = dictionary.typedDimensions.map { typedDimension ->
            DbDimensions.writeTypedDimension(
                typedDimension,
                dictionary.owner,
                ownerId,
                languageIds,
                domainLookupItems
            )
        }

        val dimensionLookupItems = explicitDimensionLookupItem + typedDimensionLookupItem

        return DpmDictionaryLookupItem(
            domainLookupItems = domainLookupItems,
            dimensionLookupItems = dimensionLookupItems,
            ownerId = ownerId
        )
    }

    fun writeDictionaryMetricsToFixedDomain(
        dpmDictionary: DpmDictionary,
        languageIds: Map<Language, EntityID<Int>>,
        dpmDictionaryLookupItem: DpmDictionaryLookupItem,
        metricDomainId: EntityID<Int>
    ): Pair<List<MemberLookupItem>, List<HierarchyLookupItem>> {

        return dpmDictionary.metricDomains.map { metricDomain ->

            val memberLookupItems = DbMetric.writeMetricDomainMembers(
                metricDomain,
                dpmDictionary.owner,
                metricDomainId,
                dpmDictionaryLookupItem.ownerId,
                languageIds,
                dpmDictionaryLookupItem.domainLookupItems
            )

            val hierarchyLookupItems = DbHierarchies.writeHierarchiesAndAndNodes(
                metricDomain.hierarchies,
                metricDomainId,
                dpmDictionaryLookupItem.ownerId,
                languageIds,
                memberLookupItems
            )

            Pair(memberLookupItems, hierarchyLookupItems)
        }.reduce { accumulator, element ->
            Pair(
                accumulator.first + element.first,
                accumulator.second + element.second
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
