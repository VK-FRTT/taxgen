package fi.vm.yti.taxgen.sqliteprovider.writers

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MetricTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object DbMetric {

    fun writeMetricDomainAndHierarchy(
        dictionaryItem: DpmDictionaryItem
    ): Pair<EntityID<Int>, EntityID<Int>> {

        val langEn = Language.findByIso6391Code("en")!!

        val metricDomainConcept = Concept(
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
            applicableFrom = null,
            applicableUntil = null,
            label = TranslatedText(mapOf(langEn to "Metrics")),
            description = TranslatedText(emptyMap()),
            owner = dictionaryItem.owner
        )

        val metricHierarchyConcept = Concept(
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
            applicableFrom = null,
            applicableUntil = null,
            label = TranslatedText(mapOf(langEn to "Metrics")),
            description = TranslatedText(emptyMap()),
            owner = dictionaryItem.owner
        )

        return transaction {

            val metricDomainConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                metricDomainConcept,
                ConceptType.DOMAIN
            )

            val metricDomainId = DomainTable.insertAndGetId {
                it[domainCodeCol] = "MET"
                it[domainLabelCol] = metricDomainConcept.label.defaultTranslation()
                it[domainDescriptionCol] = metricDomainConcept.description.defaultTranslation()
                it[domainXBRLCodeCol] = "MET"
                it[dataTypeCol] = null
                it[isTypedDomainCol] = false
                it[conceptIdCol] = metricDomainConceptId
            }

            val metricHierarchyConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                metricHierarchyConcept,
                ConceptType.HIERARCHY
            )

            val metricHierarchyId = HierarchyTable.insertAndGetId {
                it[hierarchyCodeCol] = "MET1"
                it[hierarchyLabelCol] = metricHierarchyConcept.label.defaultTranslation()
                it[hierarchyDescriptionCol] = metricHierarchyConcept.description.defaultTranslation()
                it[domainIdCol] = metricDomainId
                it[conceptIdCol] = metricHierarchyConceptId
            }

            Pair(metricDomainId, metricHierarchyId)
        }
    }

    fun writeMetric(
        dictionaryItem: DpmDictionaryItem,
        metric: Metric,
        metricDomainId: EntityID<Int>,
        metricHierarchyId: EntityID<Int>
    ) {
        transaction {

            //Metric Member
            val memberConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                metric.concept,
                ConceptType.MEMBER
            )

            val memberCode = "TODO_$metric.memberCodeNumber"
            val memberXbrlCode = "${dictionaryItem.owner.prefix}_met:$memberCode"

            val metricMemberId = MemberTable.insertAndGetId {
                it[memberCodeCol] = memberCode
                it[memberLabelCol] = metric.concept.label.defaultTranslation()
                it[memberXBRLCodeCol] = memberXbrlCode
                it[isDefaultMemberCol] = null
                it[conceptIdCol] = memberConceptId
                it[domainIdCol] = metricDomainId
            }

            //Metric HierarchyNode
            val nodeConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                metric.concept,
                ConceptType.HIERARCHY_NODE
            )

            HierarchyNodeTable.insert {
                it[hierarchyIdCol] = metricHierarchyId
                it[memberIdCol] = metricMemberId
                it[isAbstractCol] = false
                it[comparisonOperatorCol] = null
                it[unaryOperatorCol] = null
                it[orderCol] = 1 //TODO
                it[levelCol] = 1 //TODO
                it[parentMemberID] = null //TODO
                it[hierarchyNodeLabel] = metric.concept.label.defaultTranslation()
                it[conceptIdCol] = nodeConceptId
                it[pathCol] = null
            }

            //Metric
            val referencedDomain = dictionaryItem.domainItemForCode(metric.referencedDomainCode)
            val referencedDomainId = referencedDomain?.domainId

            val referencedHierarchy = referencedDomain?.hierarchyItemForCode(metric.referencedHierarchyCode)
            val referencedHierarchyId = referencedHierarchy?.hierarchyId

            MetricTable.insert {
                it[correspondingMemberCol] = metricMemberId
                it[dataTypeCol] = metric.dataType
                it[flowTypeCol] = metric.flowType
                it[balanceTypeCol] = metric.balanceType
                it[referencedDomainCol] = referencedDomainId
                it[referencedHierarchyCol] = referencedHierarchyId
                it[hierarchyStartingMemberCol] = null
                it[isStartingMemberIncludedCol] = null
            }
        }
    }
}
